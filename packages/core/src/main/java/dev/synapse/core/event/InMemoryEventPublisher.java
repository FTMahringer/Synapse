package dev.synapse.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * In-process event publisher. Wraps Spring ApplicationEventPublisher so
 * subscribers can be Spring @EventListener beans without coupling to Redis.
 */
@Component
public class InMemoryEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InMemoryEventPublisher.class);

    private final ApplicationEventPublisher springPublisher;

    public InMemoryEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    @Override
    public void publish(SynapseEvent event) {
        log.debug("Publishing event: {} from {}", event.type(), event.source());
        springPublisher.publishEvent(new SynapseSpringEvent(this, event));
    }
}
