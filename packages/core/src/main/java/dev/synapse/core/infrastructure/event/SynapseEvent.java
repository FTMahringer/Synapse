package dev.synapse.core.infrastructure.event;

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
    Instant occurredAt
) {
    public static SynapseEvent of(SynapseEventType type, String source, Map<String, Object> payload) {
        return new SynapseEvent(UUID.randomUUID(), type, source, payload, null, Instant.now());
    }

    public static SynapseEvent of(SynapseEventType type, String source, Map<String, Object> payload, UUID correlationId) {
        return new SynapseEvent(UUID.randomUUID(), type, source, payload, correlationId, Instant.now());
    }
}
