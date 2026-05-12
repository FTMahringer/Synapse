package dev.synapse.plugin.api;

import java.util.List;
import java.util.Map;

/**
 * Request to a ModelProvider for a completion or streaming response.
 */
public final class CompletionRequest {

    private final String model;
    private final List<Message> messages;
    private final double temperature;
    private final int maxTokens;
    private final Map<String, Object> extra;

    public CompletionRequest(
            String model,
            List<Message> messages,
            double temperature,
            int maxTokens,
            Map<String, Object> extra) {
        this.model = model;
        this.messages = List.copyOf(messages);
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.extra = extra != null ? Map.copyOf(extra) : Map.of();
    }

    public String getModel() { return model; }

    public List<Message> getMessages() { return messages; }

    public double getTemperature() { return temperature; }

    public int getMaxTokens() { return maxTokens; }

    /** Provider-specific extra parameters passed through without validation. */
    public Map<String, Object> getExtra() { return extra; }

    /**
     * A single message in the conversation history.
     */
    public record Message(String role, String content) {}
}
