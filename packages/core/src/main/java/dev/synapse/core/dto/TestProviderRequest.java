package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record TestProviderRequest(
    @NotNull
    UUID providerId,
    
    @NotBlank
    String model,
    
    @NotNull
    List<TestMessage> messages,
    
    Double temperature,
    
    Integer maxTokens,
    
    Boolean storePrompt
) {
    public record TestMessage(
        @NotBlank String role,
        @NotBlank String content
    ) {}
}
