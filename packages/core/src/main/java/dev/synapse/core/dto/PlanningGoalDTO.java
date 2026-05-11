package dev.synapse.core.dto;

import java.time.Instant;
import java.util.UUID;

public record PlanningGoalDTO(
    UUID id,
    String teamId,
    UUID collaborationSessionId,
    String title,
    String goalStatement,
    String createdByAgentId,
    String status,
    Instant createdAt,
    Instant updatedAt
) {}
