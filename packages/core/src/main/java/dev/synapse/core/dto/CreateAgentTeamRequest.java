package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record CreateAgentTeamRequest(
    @NotBlank(message = "Team ID is required")
    @Pattern(regexp = "^[a-z0-9-]{2,64}$", message = "Team ID must be lowercase alphanumeric with hyphens, 2-64 chars")
    String id,

    @NotBlank(message = "Team name is required")
    String name,

    String leaderAgentId,

    Map<String, Object> config
) {}
