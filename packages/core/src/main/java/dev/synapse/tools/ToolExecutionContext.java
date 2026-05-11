package dev.synapse.tools;

import java.util.UUID;

public record ToolExecutionContext(
    String agentId,
    String teamId,
    UUID collaborationSessionId,
    UUID goalId
) {}
