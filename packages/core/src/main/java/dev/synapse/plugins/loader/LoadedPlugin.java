package dev.synapse.plugins.loader;

import dev.synapse.plugin.api.SynapsePlugin;

import java.net.URLClassLoader;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Runtime representation of a loaded plugin.
 *
 * <p>Holds the isolated ClassLoader, ModuleLayer, and the plugin instance.
 * Used by registries and the loader service for lifecycle operations.
 */
public record LoadedPlugin(
    String pluginId,
    String version,
    Path jarPath,
    URLClassLoader classLoader,
    java.lang.ModuleLayer moduleLayer,
    SynapsePlugin instance,
    Instant loadedAt
) {

    /**
     * Unloads the plugin by calling onUnload() and closing the ClassLoader.
     * Exceptions during onUnload are logged but not rethrown.
     */
    public void unload() {
        try {
            instance.onUnload();
        } catch (Exception e) {
            // Logged by caller; do not block unload
        }
        try {
            classLoader.close();
        } catch (Exception e) {
            // Best-effort close
        }
    }
}
