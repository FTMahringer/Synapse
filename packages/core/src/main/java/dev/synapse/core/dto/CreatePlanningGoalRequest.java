package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreatePlanningGoalRequest(
    UUID collaborationSessionId,
    @NotBlank String title,
    @NotBlank String goalStatement,
    @NotBlank String createdByAgentId
) {}
