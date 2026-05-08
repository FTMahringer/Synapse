package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record UpdateSystemMetadataRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Version is required")
    String version,

    Map<String, Object> settings
) {}
