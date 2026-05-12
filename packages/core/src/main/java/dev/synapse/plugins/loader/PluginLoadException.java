package dev.synapse.plugins.loader;

/**
 * Exception thrown when a plugin fails to load.
 */
public class PluginLoadException extends Exception {

    private final String pluginId;

    public PluginLoadException(String pluginId, String message) {
        super(message);
        this.pluginId = pluginId;
    }

    public PluginLoadException(String pluginId, String message, Throwable cause) {
        super(message, cause);
        this.pluginId = pluginId;
    }

    public String getPluginId() {
        return pluginId;
    }
}
