package dev.synapse.core.logging;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SystemLogService {

    private final JdbcClient jdbcClient;

    public SystemLogService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void info(LogCategory category, String event, String source, String payload) {
        write(LogLevel.INFO, category, event, source, payload, null, null);
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
