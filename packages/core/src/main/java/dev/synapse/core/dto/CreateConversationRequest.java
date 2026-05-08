package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateConversationRequest(
    @NotBlank(message = "Agent ID is required")
    String agentId
) {}
