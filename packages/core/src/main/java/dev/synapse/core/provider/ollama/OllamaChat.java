package dev.synapse.core.provider.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class OllamaChat {

    public record ChatRequest(
        String model,
        List<Message> messages,
        Boolean stream,
        Options options
    ) {}

    public record Message(
        String role,
        String content
    ) {}

    public record Options(
        Double temperature,
        @JsonProperty("top_p")
        Double topP,
        @JsonProperty("top_k")
        Integer topK,
        @JsonProperty("num_predict")
        Integer numPredict
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatResponse(
        String model,
        @JsonProperty("created_at")
        String createdAt,
        Message message,
        Boolean done,
        @JsonProperty("total_duration")
        Long totalDuration,
        @JsonProperty("load_duration")
        Long loadDuration,
        @JsonProperty("prompt_eval_count")
        Integer promptEvalCount,
        @JsonProperty("prompt_eval_duration")
        Long promptEvalDuration,
        @JsonProperty("eval_count")
        Integer evalCount,
        @JsonProperty("eval_duration")
        Long evalDuration
    ) {}
}
