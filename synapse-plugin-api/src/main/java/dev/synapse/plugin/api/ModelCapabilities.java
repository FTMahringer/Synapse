package dev.synapse.plugin.api;

/**
 * Declared capabilities of a ModelProvider.
 * Core uses this to route requests and expose features in the dashboard.
 */
public final class ModelCapabilities {

    private final boolean streaming;
    private final boolean toolCalling;
    private final boolean vision;
    private final boolean embeddings;
    private final int maxContextTokens;

    public ModelCapabilities(
            boolean streaming,
            boolean toolCalling,
            boolean vision,
            boolean embeddings,
            int maxContextTokens) {
        this.streaming = streaming;
        this.toolCalling = toolCalling;
        this.vision = vision;
        this.embeddings = embeddings;
        this.maxContextTokens = maxContextTokens;
    }

    public boolean isStreaming() { return streaming; }

    public boolean isToolCalling() { return toolCalling; }

    public boolean isVision() { return vision; }

    public boolean isEmbeddings() { return embeddings; }

    public int getMaxContextTokens() { return maxContextTokens; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private boolean streaming;
        private boolean toolCalling;
        private boolean vision;
        private boolean embeddings;
        private int maxContextTokens = 128_000;

        public Builder streaming(boolean v) { this.streaming = v; return this; }
        public Builder toolCalling(boolean v) { this.toolCalling = v; return this; }
        public Builder vision(boolean v) { this.vision = v; return this; }
        public Builder embeddings(boolean v) { this.embeddings = v; return this; }
        public Builder maxContextTokens(int v) { this.maxContextTokens = v; return this; }

        public ModelCapabilities build() {
            return new ModelCapabilities(streaming, toolCalling, vision, embeddings, maxContextTokens);
        }
    }
}
