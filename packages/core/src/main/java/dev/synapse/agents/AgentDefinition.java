package dev.synapse.agents;

import java.util.List;
import java.util.Map;

public record AgentDefinition(
        String id,
        String name,
        String type,
        String path,
        List<String> files,
        Map<String, String> metadata
) {}
