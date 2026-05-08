package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record CreateAgentRequest(
    @NotBlank(message = "Agent ID is required")
    @Pattern(regexp = "^[a-z0-9-]{2,64}$", message = "Agent ID must be lowercase alphanumeric with hyphens, 2-64 chars")
    String id,

    @NotBlank(message = "Agent name is required")
    String name,

    @NotBlank(message = "Agent type is required")
    @Pattern(regexp = "^(MAIN|TEAM_MEMBER|TEAM_LEADER|FIRM_CEO|FIRM_AGENT|CUSTOM)$", 
             message = "Type must be MAIN, TEAM_MEMBER, TEAM_LEADER, FIRM_CEO, FIRM_AGENT, or CUSTOM")
    String type,

    Map<String, Object> config
) {}
