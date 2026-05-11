package dev.synapse.tools;

import java.time.Instant;
import java.util.Map;

public record ToolExecutionResponse(
    String toolId,
    String status,
    Map<String, Object> result,
    boolean cached,
    Long cacheTtlRemainingSeconds,
    Instant executedAt,
    String hardeningReasonCode,
    String enforcedMode
) {}
