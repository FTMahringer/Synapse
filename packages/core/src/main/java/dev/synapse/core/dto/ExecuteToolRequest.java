package dev.synapse.core.dto;

import java.util.Map;
import java.util.UUID;

public record ExecuteToolRequest(
    String agentId,
    String teamId,
    UUID collaborationSessionId,
    UUID goalId,
    Map<String, Object> input
) {}
