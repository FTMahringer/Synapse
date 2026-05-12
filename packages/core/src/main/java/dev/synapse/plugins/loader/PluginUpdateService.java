package dev.synapse.plugins.loader;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.common.repository.PluginRepository;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * Plugin update service: unload old, swap ClassLoader, reload new.
 *
 * <p>Update flow:
 * <ol>
 *   <li>Unload current plugin (if loaded)</li>
 *   <li>Delete old JAR from storage</li>
 *   <li>Stage new JAR</li>
 *   <li>Load new plugin</li>
 *   <li>Register in appropriate registry</li>
 * </ol>
 */
@Service
public class PluginUpdateService {

    private final PluginLoaderService loaderService;
    private final PluginStorageService storageService;
    private final PluginRepository pluginRepository;
    private final ChannelRegistry channelRegistry;
    private final ModelProviderRegistry providerRegistry;
    private final SystemLogService logService;
    private final PluginContextFactory contextFactory;

    public PluginUpdateService(
        PluginLoaderService loaderService,
        PluginStorageService storageService,
        PluginRepository pluginRepository,
        ChannelRegistry channelRegistry,
        ModelProviderRegistry providerRegistry,
        SystemLogService logService,
        PluginContextFactory contextFactory
    ) {
        this.loaderService = loaderService;
        this.storageService = storageService;
        this.pluginRepository = pluginRepository;
        this.channelRegistry = channelRegistry;
        this.providerRegistry = providerRegistry;
        this.logService = logService;
        this.contextFactory = contextFactory;
    }

    /**
     * Updates a plugin to a new JAR version.
     *
     * @param pluginId the plugin id
     * @param newJarPath path to the new JAR file
     * @return the loaded plugin record
     * @throws PluginLoadException if loading fails
     */
    public LoadedPlugin updatePlugin(String pluginId, Path newJarPath) throws PluginLoadException {
        Plugin dbPlugin = pluginRepository.findById(pluginId)
            .orElseThrow(() -> new PluginLoadException(pluginId, "Plugin not found in database"));

        logService.log(LogLevel.INFO, LogCategory.PLUGIN,
            Map.of("component", "PluginUpdateService"),
            "PLUGIN_UPDATE_START",
            Map.of("id", pluginId, "fromVersion", dbPlugin.getVersion(), "jar", newJarPath.toString()),
            null, null);

        // 1. Unload current if loaded
        if (loaderService.isLoaded(pluginId)) {
            loaderService.unloadPlugin(pluginId);
            channelRegistry.unregisterByPluginId(pluginId);
            providerRegistry.unregisterByPluginId(pluginId);
        }

        // 2. Delete old JARs
        String oldJarName = pluginId + ".jar";
        storageService.deleteJar(oldJarName);
        // Also delete any versioned JARs
        storageService.listSystemJars().stream()
            .filter(p -> p.getFileName().toString().startsWith(pluginId + "-"))
            .forEach(p -> storageService.deleteJar(p.getFileName().toString()));

        // 3. Stage new JAR
        try {
            storageService.stageJar(newJarPath);
        } catch (Exception e) {
            throw new PluginLoadException(pluginId, "Failed to stage new JAR: " + e.getMessage(), e);
        }

        // 4. Load new plugin
        Path stagedJar = storageService.getStagingDir().resolve(newJarPath.getFileName().toString());
        LoadedPlugin loaded = loaderService.loadPlugin(stagedJar, dbPlugin);

        // 5. Register in registry
        registerInRegistry(loaded, dbPlugin);

        // 6. Promote to system
        storageService.promoteToSystem(newJarPath.getFileName().toString());

        logService.log(LogLevel.INFO, LogCategory.PLUGIN,
            Map.of("component", "PluginUpdateService"),
            "PLUGIN_UPDATE_COMPLETE",
            Map.of("id", pluginId, "version", loaded.version()),
            null, null);

        return loaded;
    }

    /**
     * Checks if a config schema migration would block an update.
     *
     * @param currentManifest current plugin manifest
     * @param newManifest new plugin manifest
     * @return empty if update is allowed, otherwise the blocking reason
     */
    public Optional<String> checkConfigSchemaMigration(
        Map<String, Object> currentManifest,
        Map<String, Object> newManifest
    ) {
        @SuppressWarnings("unchecked")
        Map<String, Object> currentSchema = extractConfigSchema(currentManifest);
        @SuppressWarnings("unchecked")
        Map<String, Object> newSchema = extractConfigSchema(newManifest);

        // Find new required fields
        for (Map.Entry<String, Object> entry : newSchema.entrySet()) {
            String key = entry.getKey();
            if (!currentSchema.containsKey(key)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> fieldMeta = entry.getValue() instanceof Map<?, ?> m
                    ? (Map<String, Object>) m : Map.of();
                Object required = fieldMeta.get("required");
                if (Boolean.TRUE.equals(required)) {
                    return Optional.of("New required config field '" + key + "' added in update. " +
                        "Please configure before updating.");
                }
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractConfigSchema(Map<String, Object> manifest) {
        Object config = manifest.get("config_schema");
        if (config instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        Object config2 = manifest.get("config");
        if (config2 instanceof Map<?, ?> m2) {
            return (Map<String, Object>) m2;
        }
        return Map.of();
    }

    private void registerInRegistry(LoadedPlugin loaded, Plugin dbPlugin) {
        var instance = loaded.instance();
        if (instance instanceof dev.synapse.plugin.api.Channel channel) {
            channelRegistry.register(loaded, channel);
            try {
                channel.onInstall();
            } catch (Exception e) {
                logService.log(LogLevel.WARN, LogCategory.PLUGIN,
                    Map.of("component", "PluginUpdateService"),
                    "PLUGIN_CHANNEL_INSTALL_FAILED",
                    Map.of("id", loaded.pluginId(), "error", e.getMessage()),
                    null, null);
            }
        } else if (instance instanceof dev.synapse.plugin.api.ModelProvider provider) {
            providerRegistry.register(loaded, provider);
            try {
                provider.configure(contextFactory.createContext(dbPlugin, instance));
            } catch (Exception e) {
                logService.log(LogLevel.WARN, LogCategory.PLUGIN,
                    Map.of("component", "PluginUpdateService"),
                    "PLUGIN_PROVIDER_CONFIGURE_FAILED",
                    Map.of("id", loaded.pluginId(), "error", e.getMessage()),
                    null, null);
            }
        }
    }
}
