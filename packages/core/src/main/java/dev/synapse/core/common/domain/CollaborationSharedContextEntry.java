package dev.synapse.core.common.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "collaboration_shared_context",
    uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "context_key"})
)
public class CollaborationSharedContextEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "context_key", nullable = false)
    private String contextKey;

    @Column(name = "context_value", nullable = false, columnDefinition = "TEXT")
    private String contextValue;

    @Column(name = "updated_by_agent_id", nullable = false)
    private String updatedByAgentId;

    @Column(nullable = false)
    private Integer version = 1;

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

    public String getContextKey() {
        return contextKey;
    }

    public void setContextKey(String contextKey) {
        this.contextKey = contextKey;
    }

    public String getContextValue() {
        return contextValue;
    }

    public void setContextValue(String contextValue) {
        this.contextValue = contextValue;
    }

    public String getUpdatedByAgentId() {
        return updatedByAgentId;
    }

    public void setUpdatedByAgentId(String updatedByAgentId) {
        this.updatedByAgentId = updatedByAgentId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
