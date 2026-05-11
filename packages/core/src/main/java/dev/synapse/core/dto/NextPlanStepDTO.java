package dev.synapse.core.dto;

import java.util.Map;
import java.util.UUID;

public record NextPlanStepDTO(
    UUID goalId,
    UUID planId,
    Integer planVersion,
    Map<String, Object> nextStep
) {}
