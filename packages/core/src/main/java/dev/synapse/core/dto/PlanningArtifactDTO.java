package dev.synapse.core.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PlanningArtifactDTO(
    UUID id,
    UUID goalId,
    Integer planVersion,
    String status,
    String compactSummary,
    List<Map<String, Object>> steps,
    List<Map<String, Object>> reasoningChain,
    Integer totalSteps,
    Integer completedSteps,
    String createdByAgentId,
    Instant createdAt,
    Instant updatedAt
) {}
