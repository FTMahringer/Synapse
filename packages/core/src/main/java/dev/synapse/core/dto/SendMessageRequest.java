package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
    @NotBlank(message = "Message content is required")
    String content
) {}
