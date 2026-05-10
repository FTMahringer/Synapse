package dev.synapse.core.common.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Heartbeat record for an active agent.
 * Each heartbeat is a point-in-time pulse from the agent's runtime.
 */
@Entity
@Table(name = "agent_heartbeats")
public class AgentHeartbeat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column
    private String note;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
