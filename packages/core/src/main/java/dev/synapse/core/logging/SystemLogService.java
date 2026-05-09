package dev.synapse.core.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.synapse.core.event.EventPublisher;
import dev.synapse.core.event.SynapseEvent;
import dev.synapse.core.event.SynapseEventType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SystemLogService {

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;

    public SystemLogService(JdbcClient jdbcClient, ObjectMapper objectMapper, EventPublisher eventPublisher) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    public void info(LogCategory category, String event, String source, String payload) {
        write(LogLevel.INFO, category, event, source, payload, null, null);
    }

    public void log(LogLevel level, LogCategory category, Map<String, Object> source, String event, Map<String, Object> payload, UUID correlationId, UUID traceId) {
        write(level, category, event, toJson(source), toJson(payload), correlationId, traceId);
    }

    public void write(LogLevel level, LogCategory category, String event, String source, String payload, UUID correlationId, UUID traceId) {
        jdbcClient.sql("""
                INSERT INTO system_logs (level, category, source, event, payload, correlation_id, trace_id)
                VALUES (:level, :category, CAST(:source AS jsonb), :event, CAST(:payload AS jsonb), :correlationId, :traceId)
                """)
                .param("level", level.name())
                .param("category", category.name())
                .param("source", sanitizeJson(source))
                .param("event", event)
                .param("payload", sanitizeJson(payload))
                .param("correlationId", correlationId)
                .param("traceId", traceId)
                .update();

        eventPublisher.publish(SynapseEvent.of(
            SynapseEventType.LOG_WRITTEN,
            source != null ? source : "system",
            Map.of("level", level.name(), "category", category.name(), "event", event),
            correlationId
        ));
    }

    private String toJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public List<SystemLog> latest(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 250));
        return jdbcClient.sql("""
                SELECT id, timestamp, level, category, source::text, event, payload::text, correlation_id, trace_id
                FROM system_logs
                ORDER BY timestamp DESC
                LIMIT :limit
                """)
                .param("limit", safeLimit)
                .query(SystemLogService::mapRow)
                .list();
    }

    private static SystemLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new SystemLog(
                rs.getObject("id", UUID.class),
                rs.getTimestamp("timestamp").toInstant(),
                LogLevel.valueOf(rs.getString("level")),
                LogCategory.valueOf(rs.getString("category")),
                rs.getString("source"),
                rs.getString("event"),
                rs.getString("payload"),
                rs.getObject("correlation_id", UUID.class),
                rs.getObject("trace_id", UUID.class)
        );
    }

    private static String sanitizeJson(String value) {
        if (value == null || value.isBlank()) {
            return "{}";
        }
        return value;
    }
}
