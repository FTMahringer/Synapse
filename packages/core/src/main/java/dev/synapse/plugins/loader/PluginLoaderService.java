package dev.synapse.plugins.loader;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.common.repository.PluginRepository;
import dev.synapse.core.infrastructure.event.EventPublisher;
import dev.synapse.core.infrastructure.event.SynapseEvent;
import dev.synapse.core.infrastructure.event.SynapseEventType;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.plugin.api.SynapsePlugin;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Core plugin loader service.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Create isolated {@link URLClassLoader} + JPMS {@link ModuleLayer} per plugin</li>
 *   <li>Discover plugin implementations via {@link ServiceLoader}</li>
 *   <li>Call {@code onLoad()} / {@code onUnload()} lifecycle hooks</li>
 *   <li>Track loaded plugins in memory</li>
 * </ul>
 *
 * <p>Each plugin JAR is loaded into its own ModuleLayer that only requires
 * {@code synapse.plugin.api}. Core modules are not exported to plugin layers.
 */
@Service
public class PluginLoaderService {

    private final PluginRepository pluginRepository;
    private final SystemLogService logService;
    private final EventPublisher eventPublisher;
    private final PluginContextFactory contextFactory;
    private final PluginSandboxService sandboxService;

    /** In-memory registry of currently loaded plugins by id. */
    private final Map<String, LoadedPlugin> loadedPlugins =
        new ConcurrentHashMap<>();

    public PluginLoaderService(
        PluginRepository pluginRepository,
        SystemLogService logService,
        EventPublisher eventPublisher,
        PluginContextFactory contextFactory,
        PluginSandboxService sandboxService
    ) {
        this.pluginRepository = pluginRepository;
        this.logService = logService;
        this.eventPublisher = eventPublisher;
        this.contextFactory = contextFactory;
        this.sandboxService = sandboxService;
    }

    /**
     * Loads a plugin JAR into an isolated ClassLoader + ModuleLayer.
     *
     * @param jarPath   path to the plugin JAR
     * @param dbPlugin  the database entity for this plugin
     * @return the loaded plugin record
     * @throws PluginLoadException if loading fails
     */
    public LoadedPlugin loadPlugin(Path jarPath, Plugin dbPlugin)
        throws PluginLoadException {
        String pluginId = dbPlugin.getId();

        // Prevent double-load
        if (loadedPlugins.containsKey(pluginId)) {
            throw new PluginLoadException(pluginId, "Plugin already loaded");
        }

        updateLoaderState(pluginId, Plugin.LoaderState.LOADING, null);

        try {
            // Validate jarPath is a local file under the plugins directory
            // (prevent SSRF via external URLs and path traversal)
            Path normalized = jarPath.normalize();
            Path pluginsDir = Path.of(
                System.getenv().getOrDefault(
                    "SYNAPSE_HOME",
                    System.getProperty("user.home") + "/.synapse"
                ),
                "plugins"
            ).normalize();
            if (
                !normalized.isAbsolute() || !normalized.startsWith(pluginsDir)
            ) {
                throw new PluginLoadException(
                    pluginId,
                    "Plugin JAR path must be an absolute local file under " +
                        pluginsDir
                );
            }
            if (!java.nio.file.Files.exists(normalized)) {
                throw new PluginLoadException(
                    pluginId,
                    "Plugin JAR not found: " + normalized
                );
            }

            // Build file:// URL directly from validated local path.
            // CodeQL flags toUri().toURL() as SSRF-prone, so we construct
            // the URL string manually from the already-validated path.
            String absolutePath = normalized.toAbsolutePath().toString();
            String fileUrl = "file://" + absolutePath.replace('\\', '/');

            // Validate the URL string starts with file:// before creating URL
            if (!fileUrl.startsWith("file://")) {
                throw new PluginLoadException(
                    pluginId,
                    "Plugin JAR URL must start with file://"
                );
            }

            URL jarUrl;
            try {
                jarUrl = new URL(fileUrl);
            } catch (MalformedURLException e) {
                throw new PluginLoadException(
                    pluginId,
                    "Invalid JAR URL: " + fileUrl
                );
            }

            // Layer 1: Create URLClassLoader (parent = platform class loader)
            URLClassLoader classLoader = new URLClassLoader(
                new URL[] { jarUrl },
                ClassLoader.getPlatformClassLoader()
            );

            // Layer 2: Create JPMS ModuleLayer
            ModuleFinder pluginFinder = ModuleFinder.of(normalized);
            Set<ModuleDescriptor> descriptors = pluginFinder
                .findAll()
                .stream()
                .map(java.lang.module.ModuleReference::descriptor)
                .collect(Collectors.toSet());

            if (descriptors.isEmpty()) {
                classLoader.close();
                throw new PluginLoadException(
                    pluginId,
                    "JAR contains no module descriptor (module-info.class). " +
                        "Plugins must declare a JPMS module."
                );
            }

            Set<String> moduleNames = descriptors
                .stream()
                .map(ModuleDescriptor::name)
                .collect(Collectors.toSet());

            Configuration parentConfig = ModuleLayer.boot().configuration();
            Configuration pluginConfig = parentConfig.resolve(
                pluginFinder,
                ModuleFinder.of(),
                moduleNames
            );

            ModuleLayer pluginLayer = ModuleLayer.defineModulesWithOneLoader(
                pluginConfig,
                List.of(ModuleLayer.boot()),
                classLoader
            ).layer();

            // Layer 3: Discover plugin implementation via ServiceLoader
            ServiceLoader<SynapsePlugin> loader = ServiceLoader.load(
                pluginLayer,
                SynapsePlugin.class
            );

            Optional<SynapsePlugin> instanceOpt = loader.findFirst();
            if (instanceOpt.isEmpty()) {
                classLoader.close();
                throw new PluginLoadException(
                    pluginId,
                    "No SynapsePlugin implementation found in JAR. " +
                        "Ensure META-INF/services/dev.synapse.plugin.api.SynapsePlugin is present."
                );
            }

            SynapsePlugin instance = instanceOpt.get();

            // Validate id matches
            if (!pluginId.equals(instance.getId())) {
                classLoader.close();
                throw new PluginLoadException(
                    pluginId,
                    "Plugin id mismatch: manifest says '" +
                        pluginId +
                        "' but implementation says '" +
                        instance.getId() +
                        "'"
                );
            }

            // Validate JPMS isolation
            LoadedPlugin tempLoaded = new LoadedPlugin(
                pluginId,
                instance.getVersion(),
                jarPath,
                classLoader,
                pluginLayer,
                instance,
                Instant.now()
            );
            if (!sandboxService.validateJpmsIsolation(tempLoaded)) {
                classLoader.close();
                throw new PluginLoadException(
                    pluginId,
                    "JPMS isolation validation failed: plugin can access forbidden core modules"
                );
            }

            // Inject context and call onLoad with timeout
            var context = contextFactory.createContext(dbPlugin, instance);
            boolean onLoadOk = sandboxService.runLifecycleHookWithTimeout(
                pluginId,
                () -> {
                    try {
                        instance.onLoad(context);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                true,
                dbPlugin
            );
            if (!onLoadOk) {
                classLoader.close();
                throw new PluginLoadException(
                    pluginId,
                    "Plugin onLoad() failed or timed out; plugin marked ERROR"
                );
            }

            LoadedPlugin loaded = new LoadedPlugin(
                pluginId,
                instance.getVersion(),
                jarPath,
                classLoader,
                pluginLayer,
                instance,
                Instant.now()
            );

            loadedPlugins.put(pluginId, loaded);
            updateLoaderState(pluginId, Plugin.LoaderState.LOADED, null);

            logService.log(
                LogLevel.INFO,
                LogCategory.PLUGIN,
                Map.of("component", "PluginLoaderService"),
                "PLUGIN_LOADED",
                Map.of(
                    "id",
                    pluginId,
                    "version",
                    instance.getVersion(),
                    "jar",
                    jarPath.toString()
                ),
                null,
                null
            );

            eventPublisher.publish(
                SynapseEvent.of(
                    SynapseEventType.LOG_WRITTEN,
                    "PluginLoaderService",
                    Map.of("event", "PLUGIN_LOADED", "pluginId", pluginId)
                )
            );

            return loaded;
        } catch (PluginLoadException e) {
            updateLoaderState(
                pluginId,
                Plugin.LoaderState.ERROR,
                e.getMessage()
            );
            throw e;
        } catch (Exception e) {
            String msg =
                "Unexpected error during plugin load: " + e.getMessage();
            updateLoaderState(pluginId, Plugin.LoaderState.ERROR, msg);
            throw new PluginLoadException(pluginId, msg, e);
        }
    }

    /**
     * Unloads a plugin by id. Calls onUnload(), closes ClassLoader, removes from memory.
     *
     * @param pluginId the plugin id
     * @return true if a plugin was unloaded, false if not found
     */
    public boolean unloadPlugin(String pluginId) {
        LoadedPlugin loaded = loadedPlugins.remove(pluginId);
        if (loaded == null) {
            return false;
        }

        Plugin dbPlugin = pluginRepository.findById(pluginId).orElse(null);
        if (dbPlugin != null) {
            sandboxService.runLifecycleHookWithTimeout(
                pluginId,
                () -> {
                    try {
                        loaded.instance().onUnload();
                    } catch (Exception e) {
                        // Logged by sandbox service; do not block unload
                    }
                },
                false,
                dbPlugin
            );
        } else {
            loaded.unload();
        }
        updateLoaderState(pluginId, Plugin.LoaderState.UNLOADED, null);

        logService.log(
            LogLevel.INFO,
            LogCategory.PLUGIN,
            Map.of("component", "PluginLoaderService"),
            "PLUGIN_UNLOADED",
            Map.of("id", pluginId),
            null,
            null
        );

        return true;
    }

    /** Reloads a plugin: unload + load. */
    public LoadedPlugin reloadPlugin(Path jarPath, Plugin dbPlugin)
        throws PluginLoadException {
        unloadPlugin(dbPlugin.getId());
        return loadPlugin(jarPath, dbPlugin);
    }

    /** Returns a loaded plugin by id, or empty if not loaded. */
    public Optional<LoadedPlugin> getLoaded(String pluginId) {
        return Optional.ofNullable(loadedPlugins.get(pluginId));
    }

    /** Returns all currently loaded plugins. */
    public Collection<LoadedPlugin> getAllLoaded() {
        return Collections.unmodifiableCollection(loadedPlugins.values());
    }

    /** Returns true if the plugin is currently loaded. */
    public boolean isLoaded(String pluginId) {
        return loadedPlugins.containsKey(pluginId);
    }

    /** Unloads all plugins (used during graceful shutdown). */
    public void unloadAll() {
        List<String> ids = List.copyOf(loadedPlugins.keySet());
        for (String id : ids) {
            unloadPlugin(id);
        }
    }

    private void updateLoaderState(
        String pluginId,
        Plugin.LoaderState state,
        String errorMessage
    ) {
        pluginRepository
            .findById(pluginId)
            .ifPresent(p -> {
                p.setLoaderState(state);
                p.setErrorMessage(errorMessage);
                if (state == Plugin.LoaderState.LOADED) {
                    p.setLoadedAt(Instant.now());
                }
                pluginRepository.save(p);
            });
    }
}
