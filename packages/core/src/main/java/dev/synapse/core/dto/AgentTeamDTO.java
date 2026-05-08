package dev.synapse.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentTeamDTO(
    String id,
    String name,
    String leaderAgentId,
    Map<String, Object> config,
    Instant createdAt
) {}
