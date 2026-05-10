package dev.synapse.providers.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class OllamaModels {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ModelsResponse(
        List<ModelInfo> models
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ModelInfo(
        String name,
        String model,
        Long size,
        String digest,
        Details details
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Details(
        String format,
        String family,
        List<String> families,
        String parameter_size,
        String quantization_level
    ) {}
}
