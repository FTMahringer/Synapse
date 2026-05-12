package dev.synapse.plugins.loader;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.common.repository.PluginRepository;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.plugin.api.Channel;
import dev.synapse.plugin.api.ModelProvider;
import jakarta.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Scans plugin directories on startup and loads all plugins from system/.
 *
 * <p>Startup sequence:
 * <ol>
 *   <li>Check for orphaned staging JARs (crash recovery)</li>
 *   <li>Scan system/ for JAR files</li>
 *   <li>For each JAR: find DB record → load via PluginLoaderService → register in registries</li>
 *   <li>Log summary of loaded / failed plugins</li>
 * </ol>
 */
@Component
public class StartupPluginScanner {

    private final PluginStorageService storageService;
    private final PluginLoaderService loaderService;
    private final PluginRepository pluginRepository;
    private final ChannelRegistry channelRegistry;
    private final ModelProviderRegistry providerRegistry;
    private final SystemLogService logService;
    private final PluginContextFactory contextFactory;

    public StartupPluginScanner(
        PluginStorageService storageService,
        PluginLoaderService loaderService,
        PluginRepository pluginRepository,
        ChannelRegistry channelRegistry,
        ModelProviderRegistry providerRegistry,
        SystemLogService logService,
        PluginContextFactory contextFactory
    ) {
        this.storageService = storageService;
        this.loaderService = loaderService;
        this.pluginRepository = pluginRepository;
        this.channelRegistry = channelRegistry;
        this.providerRegistry = providerRegistry;
        this.logService = logService;
        this.contextFactory = contextFactory;
    }

    @PostConstruct
    public void scanOnStartup() {
        // Crash recovery: warn about orphaned staging JARs
        if (storageService.hasOrphanedStagingJars()) {
            List<Path> orphaned = storageService.listStagingJars();
            logService.log(
                LogLevel.WARN,
                LogCategory.PLUGIN,
                Map.of("component", "StartupPluginScanner"),
                "PLUGIN_STAGING_ORPHANS",
                Map.of(
                    "count",
                    orphaned.size(),
                    "jars",
                    orphaned.stream().map(Path::toString).toList()
                ),
                null,
                null
            );
        }

        List<Path> systemJars = storageService.listSystemJars();
        if (systemJars.isEmpty()) {
            logService.log(
                LogLevel.INFO,
                LogCategory.PLUGIN,
                Map.of("component", "StartupPluginScanner"),
                "PLUGIN_STARTUP_NO_JARS",
                Map.of("dir", storageService.getSystemDir().toString()),
                null,
                null
            );
            return;
        }

        int loaded = 0;
        int failed = 0;

        for (Path jar : systemJars) {
            Optional<Plugin> dbOpt = findPluginForJar(jar);
            if (dbOpt.isEmpty()) {
                logService.log(
                    LogLevel.WARN,
                    LogCategory.PLUGIN,
                    Map.of("component", "StartupPluginScanner"),
                    "PLUGIN_STARTUP_NO_DB_RECORD",
                    Map.of("jar", jar.getFileName().toString()),
                    null,
                    null
                );
                failed++;
                continue;
            }

            Plugin dbPlugin = dbOpt.get();
            if (dbPlugin.getStatus() == Plugin.PluginStatus.DISABLED) {
                logService.log(
                    LogLevel.INFO,
                    LogCategory.PLUGIN,
                    Map.of("component", "StartupPluginScanner"),
                    "PLUGIN_STARTUP_SKIPPED_DISABLED",
                    Map.of("id", dbPlugin.getId()),
                    null,
                    null
                );
                continue;
            }

            try {
                LoadedPlugin loadedPlugin = loaderService.loadPlugin(
                    jar,
                    dbPlugin
                );
                registerInRegistry(loadedPlugin, dbPlugin);
                loaded++;
            } catch (PluginLoadException e) {
                logService.log(
                    LogLevel.ERROR,
                    LogCategory.PLUGIN,
                    Map.of("component", "StartupPluginScanner"),
                    "PLUGIN_STARTUP_LOAD_FAILED",
                    Map.of("id", dbPlugin.getId(), "error", e.getMessage()),
                    e,
                    null
                );
                failed++;
            }
        }

        logService.log(
            LogLevel.INFO,
            LogCategory.PLUGIN,
            Map.of("component", "StartupPluginScanner"),
            "PLUGIN_STARTUP_COMPLETE",
            Map.of(
                "loaded",
                loaded,
                "failed",
                failed,
                "total",
                systemJars.size()
            ),
            null,
            null
        );
    }

    private Optional<Plugin> findPluginForJar(Path jar) {
        String jarName = jar.getFileName().toString();
        // Try to match by id from DB — jar name may be "plugin-id-version.jar"
        // First try exact id match
        List<Plugin> all = pluginRepository.findAll();
        for (Plugin p : all) {
            if (
                jarName.startsWith(p.getId() + "-") ||
                jarName.startsWith(p.getId() + "_")
            ) {
                return Optional.of(p);
            }
        }
        // Fallback: try exact id as jar name without extension
        String idFromJar = jarName
            .replaceAll("-(\\d+\\.)+.*\\.jar$", "")
            .replace(".jar", "");
        return pluginRepository.findById(idFromJar);
    }

    private void registerInRegistry(LoadedPlugin loaded, Plugin dbPlugin) {
        var instance = loaded.instance();
        if (instance instanceof Channel channel) {
            try {
                channelRegistry.register(loaded, channel);
                channel.onInstall();
            } catch (Exception e) {
                logService.log(
                    LogLevel.WARN,
                    LogCategory.PLUGIN,
                    Map.of("component", "StartupPluginScanner"),
                    "PLUGIN_CHANNEL_INSTALL_FAILED",
                    Map.of("id", loaded.pluginId(), "error", e.getMessage()),
                    e,
                    null
                );
            }
        } else if (instance instanceof ModelProvider provider) {
            try {
                providerRegistry.register(loaded, provider);
                provider.configure(
                    contextFactory.createContext(dbPlugin, instance)
                );
            } catch (Exception e) {
                logService.log(
                    LogLevel.WARN,
                    LogCategory.PLUGIN,
                    Map.of("component", "StartupPluginScanner"),
                    "PLUGIN_PROVIDER_CONFIGURE_FAILED",
                    Map.of("id", loaded.pluginId(), "error", e.getMessage()),
                    e,
                    null
                );
            }
        }
    }
}
