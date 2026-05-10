package dev.synapse.core.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.synapse.core.infrastructure.event.SynapseEvent;
import dev.synapse.core.infrastructure.event.SynapseEventType;
import dev.synapse.core.infrastructure.event.SynapseSpringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Broadcasts LOG_WRITTEN events to all active SSE connections.
 */
@Component
public class SseLogBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(SseLogBroadcaster.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    public SseLogBroadcaster(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    @Async
    @EventListener
    public void onSynapseEvent(SynapseSpringEvent springEvent) {
        SynapseEvent event = springEvent.getSynapseEvent();
        if (event.type() != SynapseEventType.LOG_WRITTEN) return;

        String data;
        try {
            data = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("log")
                    .data(data));
            } catch (IOException e) {
                emitters.remove(emitter);
                log.debug("SSE emitter removed after send failure");
            }
        }
    }

    public int activeConnections() {
        return emitters.size();
    }
}
