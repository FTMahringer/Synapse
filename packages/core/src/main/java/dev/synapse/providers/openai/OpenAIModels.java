package dev.synapse.providers.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class OpenAIModels {

    public record ChatRequest(
        String model,
        List<Message> messages,
        Double temperature,
        @JsonProperty("max_tokens")
        Integer maxTokens,
        @JsonProperty("top_p")
        Double topP,
        @JsonProperty("frequency_penalty")
        Double frequencyPenalty,
        @JsonProperty("presence_penalty")
        Double presencePenalty,
        Boolean stream
    ) {}

    public record Message(
        String role,
        String content
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatResponse(
        String id,
        String object,
        Long created,
        String model,
        List<Choice> choices,
        Usage usage
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(
        Integer index,
        Message message,
        @JsonProperty("finish_reason")
        String finishReason
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
        @JsonProperty("prompt_tokens")
        Integer promptTokens,
        @JsonProperty("completion_tokens")
        Integer completionTokens,
        @JsonProperty("total_tokens")
        Integer totalTokens
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ModelsResponse(
        String object,
        List<ModelInfo> data
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ModelInfo(
        String id,
        String object,
        Long created,
        @JsonProperty("owned_by")
        String ownedBy
    ) {}
}
