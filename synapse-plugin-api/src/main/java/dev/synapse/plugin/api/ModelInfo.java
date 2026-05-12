package dev.synapse.plugin.api;

/**
 * Metadata for a single model returned by {@link ModelProvider#listModels()}.
 */
public final class ModelInfo {

    private final String id;
    private final String displayName;
    private final int contextWindow;
    private final boolean deprecated;

    public ModelInfo(String id, String displayName, int contextWindow, boolean deprecated) {
        this.id = id;
        this.displayName = displayName;
        this.contextWindow = contextWindow;
        this.deprecated = deprecated;
    }

    public String getId() { return id; }

    public String getDisplayName() { return displayName; }

    public int getContextWindow() { return contextWindow; }

    public boolean isDeprecated() { return deprecated; }
}
