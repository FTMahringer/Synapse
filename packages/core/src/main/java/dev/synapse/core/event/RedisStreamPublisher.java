package dev.synapse.core.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fans out SynapseEvents to Redis Streams for cross-process delivery.
 * Listens to in-process Spring events and writes to the synapse:events stream.
 */
@Component
public class RedisStreamPublisher {

    static final String STREAM_KEY = "synapse:events";
    static final String LOG_STREAM_KEY = "synapse:logs";

    private static final Logger log = LoggerFactory.getLogger(RedisStreamPublisher.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisStreamPublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Async
    @EventListener
    public void onSynapseEvent(SynapseSpringEvent springEvent) {
        SynapseEvent event = springEvent.getSynapseEvent();
        try {
            String json = objectMapper.writeValueAsString(event);
            Map<String, String> body = Map.of(
                "type", event.type().name(),
                "source", event.source() != null ? event.source() : "",
                "correlationId", event.correlationId() != null ? event.correlationId().toString() : "",
                "occurredAt", event.occurredAt().toString(),
                "data", json
            );

            redisTemplate.opsForStream().add(STREAM_KEY, body);

            if (event.type() == SynapseEventType.LOG_WRITTEN) {
                redisTemplate.opsForStream().add(LOG_STREAM_KEY, body);
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize event for Redis stream: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Redis stream publish failed (non-fatal): {}", e.getMessage());
        }
    }
}
