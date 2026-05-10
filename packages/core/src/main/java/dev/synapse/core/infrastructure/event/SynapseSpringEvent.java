package dev.synapse.core.infrastructure.event;

import org.springframework.context.ApplicationEvent;

/**
 * Spring ApplicationEvent wrapper for SynapseEvent.
 * Allows @EventListener beans to receive platform events.
 */
public class SynapseSpringEvent extends ApplicationEvent {

    private final SynapseEvent synapseEvent;

    public SynapseSpringEvent(Object source, SynapseEvent synapseEvent) {
        super(source);
        this.synapseEvent = synapseEvent;
    }

    public SynapseEvent getSynapseEvent() {
        return synapseEvent;
    }
}
