package dev.synapse.core.dto;

import java.time.Instant;
import java.util.UUID;

public record FirmProjectDTO(
    UUID id,
    String firmId,
    String title,
    String description,
    String status,
    String dispatchedByAgentId,
    String assignedTeamId,
    UUID conversationId,
    UUID taskId,
    Instant createdAt,
    Instant updatedAt
) {}
