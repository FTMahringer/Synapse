package dev.synapse.tools;

import java.util.Map;

public record ToolDefinition(
    String toolId,
    String displayName,
    String description,
    Map<String, Object> inputSchema,
    boolean cacheable,
    long defaultTtlSeconds
) {}
