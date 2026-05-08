package dev.synapse.core.provider.anthropic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AnthropicModels {

    public record ChatRequest(
        String model,
        @JsonProperty("max_tokens")
        Integer maxTokens,
        List<Message> messages,
        Double temperature,
        @JsonProperty("top_p")
        Double topP,
        @JsonProperty("top_k")
        Integer topK,
        Boolean stream
    ) {}

    public record Message(
        String role,
        String content
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatResponse(
        String id,
        String type,
        String role,
        List<ContentBlock> content,
        String model,
        @JsonProperty("stop_reason")
        String stopReason,
        @JsonProperty("stop_sequence")
        String stopSequence,
        Usage usage
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentBlock(
        String type,
        String text
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
        @JsonProperty("input_tokens")
        Integer inputTokens,
        @JsonProperty("output_tokens")
        Integer outputTokens
    ) {}

    public record ErrorResponse(
        String type,
        Error error
    ) {}

    public record Error(
        String type,
        String message
    ) {}
}
