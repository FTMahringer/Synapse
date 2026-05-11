package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;

public record UpsertSharedContextRequest(
    @NotBlank String value,
    @NotBlank String updatedByAgentId
) {}
