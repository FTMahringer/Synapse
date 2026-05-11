package dev.synapse.core.dto;

import java.time.Instant;
import java.util.Map;

public record ToolExecutionResponseDTO(
    String toolId,
    String status,
    Map<String, Object> result,
    boolean cached,
    Long cacheTtlRemainingSeconds,
    Instant executedAt,
    String hardeningReasonCode,
    String enforcedMode
) {}
