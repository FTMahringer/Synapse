package dev.synapse.core.common.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "collaboration_delegations")
public class CollaborationDelegation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "from_agent_id", nullable = false)
    private String fromAgentId;

    @Column(name = "to_agent_id", nullable = false)
    private String toAgentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DelegationStatus status = DelegationStatus.PENDING;

    @Column(name = "delegation_note", columnDefinition = "TEXT")
    private String delegationNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum DelegationStatus {
        PENDING, ACCEPTED, COMPLETED, REJECTED, CANCELLED
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public String getFromAgentId() {
        return fromAgentId;
    }

    public void setFromAgentId(String fromAgentId) {
        this.fromAgentId = fromAgentId;
    }

    public String getToAgentId() {
        return toAgentId;
    }

    public void setToAgentId(String toAgentId) {
        this.toAgentId = toAgentId;
    }

    public DelegationStatus getStatus() {
        return status;
    }

    public void setStatus(DelegationStatus status) {
        this.status = status;
    }

    public String getDelegationNote() {
        return delegationNote;
    }

    public void setDelegationNote(String delegationNote) {
        this.delegationNote = delegationNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
