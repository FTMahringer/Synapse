package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;

public record WriteMemoryRequest(
    @NotBlank String key,
    @NotBlank String value,
    String namespace
) {}
