package dev.synapse.plugins.loader;

import java.util.List;

/**
 * Exception thrown when plugin dependency resolution fails.
 */
public class DependencyResolutionException extends Exception {

    private final String pluginId;
    private final ResolutionFailureType failureType;
    private final List<String> details;

    public enum ResolutionFailureType {
        MISSING_DEPENDENCY,
        VERSION_MISMATCH,
        CYCLE_DETECTED,
        SLOT_CLASH,
        ALREADY_INSTALLED_OLDER,
        CONFIG_SCHEMA_INCOMPATIBLE
    }

    public DependencyResolutionException(String pluginId, ResolutionFailureType failureType, String message) {
        super(message);
        this.pluginId = pluginId;
        this.failureType = failureType;
        this.details = List.of();
    }

    public DependencyResolutionException(String pluginId, ResolutionFailureType failureType, String message, List<String> details) {
        super(message);
        this.pluginId = pluginId;
        this.failureType = failureType;
        this.details = details;
    }

    public String getPluginId() {
        return pluginId;
    }

    public ResolutionFailureType getFailureType() {
        return failureType;
    }

    public List<String> getDetails() {
        return details;
    }
}
