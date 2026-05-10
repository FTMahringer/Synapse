package dev.synapse.core.infrastructure.logging;

import java.time.Instant;
import java.util.UUID;

public record SystemLog(
        UUID id,
        Instant timestamp,
        LogLevel level,
        LogCategory category,
        String source,
        String event,
        String payload,
        UUID correlationId,
        UUID traceId
) {}
