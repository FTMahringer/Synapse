package dev.synapse.tools;

import java.time.Instant;
import java.util.Map;

public record ToolCacheEntry(
    Map<String, Object> result,
    Instant expiresAt
) {}
