package dev.synapse.core.dto;

import java.util.Map;

public record ToolDefinitionDTO(
    String toolId,
    String displayName,
    String description,
    Map<String, Object> inputSchema,
    boolean cacheable,
    long defaultTtlSeconds
) {}
