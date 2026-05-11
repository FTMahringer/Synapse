package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateCollaborationSessionRequest(
    @NotBlank String initiatedByAgentId,
    UUID conversationId
) {}
