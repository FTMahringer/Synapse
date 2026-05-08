package dev.synapse.core.dto;

import java.util.Map;

public record TestProviderResponse(
    boolean success,
    String providerName,
    String providerType,
    String model,
    Long latencyMs,
    Integer promptTokens,
    Integer completionTokens,
    Integer totalTokens,
    String responsePreview,
    String errorMessage,
    Map<String, Object> metadata
) {}
