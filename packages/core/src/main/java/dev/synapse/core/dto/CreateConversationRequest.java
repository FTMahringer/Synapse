package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateConversationRequest(
    @NotBlank(message = "Agent ID is required")
    String agentId,

    @NotNull(message = "User ID is required")
    UUID userId,

    UUID channelId
) {}
