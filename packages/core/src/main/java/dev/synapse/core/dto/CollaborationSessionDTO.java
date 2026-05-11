package dev.synapse.core.dto;

import java.time.Instant;
import java.util.UUID;

public record CollaborationSessionDTO(
    UUID id,
    String teamId,
    String initiatedByAgentId,
    UUID conversationId,
    String status,
    Instant createdAt,
    Instant updatedAt
) {}
