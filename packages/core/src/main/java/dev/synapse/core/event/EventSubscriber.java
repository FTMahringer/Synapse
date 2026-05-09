package dev.synapse.core.event;

/**
 * Abstraction for receiving platform events.
 */
public interface EventSubscriber {
    void onEvent(SynapseEvent event);

    default boolean accepts(SynapseEventType type) {
        return true;
    }
}
