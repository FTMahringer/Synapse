package dev.synapse.core.infrastructure.event;

import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Platform event emitted by services and delivered to subscribers.
 */
public record SynapseEvent(
    UUID id,
    SynapseEventType type,
    String source,
    Map<String, Object> payload,
    UUID correlationId,
    String traceId,
    String spanId,
    Instant occurredAt
) {
    public static SynapseEvent of(SynapseEventType type, String source, Map<String, Object> payload) {
        return new SynapseEvent(
            UUID.randomUUID(),
            type,
            source,
            payload,
            correlationIdFromMdcOrNull(),
            MDC.get("traceId"),
            MDC.get("spanId"),
            Instant.now()
        );
    }

    public static SynapseEvent of(SynapseEventType type, String source, Map<String, Object> payload, UUID correlationId) {
        return new SynapseEvent(
            UUID.randomUUID(),
            type,
            source,
            payload,
            correlationId != null ? correlationId : correlationIdFromMdcOrNull(),
            MDC.get("traceId"),
            MDC.get("spanId"),
            Instant.now()
        );
    }

    public static SynapseEvent of(
        SynapseEventType type,
        String source,
        Map<String, Object> payload,
        UUID correlationId,
        String traceId,
        String spanId
    ) {
        return new SynapseEvent(UUID.randomUUID(), type, source, payload, correlationId, traceId, spanId, Instant.now());
    }

    private static UUID correlationIdFromMdcOrNull() {
        String raw = MDC.get("correlationId");
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
