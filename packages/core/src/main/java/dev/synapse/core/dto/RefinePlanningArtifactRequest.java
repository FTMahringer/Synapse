package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RefinePlanningArtifactRequest(
    @NotNull UUID basePlanId,
    @NotBlank String compactSummary,
    @NotNull List<Map<String, Object>> steps,
    List<Map<String, Object>> reasoningChain,
    @NotBlank String createdByAgentId
) {}
