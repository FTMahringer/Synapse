package dev.synapse.core.event;

/**
 * Abstraction for publishing platform events.
 * Decouples producers from delivery mechanism (in-memory, Redis, etc).
 */
public interface EventPublisher {
    void publish(SynapseEvent event);
}
