package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record CreateMessageRequest(
    @NotNull(message = "Conversation ID is required")
    UUID conversationId,

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(USER|ASSISTANT|SYSTEM)$", message = "Role must be USER, ASSISTANT, or SYSTEM")
    String role,

    @NotBlank(message = "Content is required")
    String content,

    Integer tokens
) {}
