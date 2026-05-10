package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;

public record PromoteMemoryRequest(
    @NotBlank String targetTier,
    @NotBlank String reason
) {}
