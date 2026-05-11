package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

public record CreatePlanningArtifactRequest(
    @NotBlank String compactSummary,
    @NotEmpty List<Map<String, Object>> steps,
    List<Map<String, Object>> reasoningChain,
    @NotBlank String createdByAgentId
) {}
