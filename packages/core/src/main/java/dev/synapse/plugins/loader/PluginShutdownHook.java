package dev.synapse.plugins.loader;

import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Graceful shutdown hook for the plugin system.
 *
 * <p>On shutdown:
 * <ol>
 *   <li>Unload all loaded plugins (call onUnload(), close ClassLoaders)</li>
 *   <li>Migrate all JARs from staging/ to system/</li>
 * </ol>
 */
@Component
public class PluginShutdownHook {

    private final PluginLoaderService loaderService;
    private final PluginStorageService storageService;
    private final SystemLogService logService;

    public PluginShutdownHook(
        PluginLoaderService loaderService,
        PluginStorageService storageService,
        SystemLogService logService
    ) {
        this.loaderService = loaderService;
        this.storageService = storageService;
        this.logService = logService;
    }

    @PreDestroy
    public void onShutdown() {
        logService.log(LogLevel.INFO, LogCategory.PLUGIN,
            Map.of("component", "PluginShutdownHook"),
            "PLUGIN_SHUTDOWN_START",
            Map.of(),
            null, null);

        // 1. Unload all plugins gracefully
        loaderService.unloadAll();

        // 2. Promote staging → system
        storageService.promoteAllStaging();

        logService.log(LogLevel.INFO, LogCategory.PLUGIN,
            Map.of("component", "PluginShutdownHook"),
            "PLUGIN_SHUTDOWN_COMPLETE",
            Map.of(),
            null, null);
    }
}
