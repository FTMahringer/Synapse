package dev.synapse.plugin.api;

/**
 * Response from a ModelProvider completion call.
 */
public final class CompletionResponse {

    private final String content;
    private final String model;
    private final int promptTokens;
    private final int completionTokens;
    private final String finishReason;

    public CompletionResponse(
            String content,
            String model,
            int promptTokens,
            int completionTokens,
            String finishReason) {
        this.content = content;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.finishReason = finishReason;
    }

    public String getContent() { return content; }

    public String getModel() { return model; }

    public int getPromptTokens() { return promptTokens; }

    public int getCompletionTokens() { return completionTokens; }

    /** Provider-specific finish reason string, e.g. {@code "stop"}, {@code "length"}. */
    public String getFinishReason() { return finishReason; }

    public int getTotalTokens() { return promptTokens + completionTokens; }
}
