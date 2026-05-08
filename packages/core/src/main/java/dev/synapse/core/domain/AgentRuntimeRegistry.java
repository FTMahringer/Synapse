package dev.synapse.core.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Tracks runtime state and activation status of agents.
 * Separate from Agent entity to track dynamic runtime state.
 */
@Entity
@Table(name = "agent_runtime_registry")
public class AgentRuntimeRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AgentActivationState state = AgentActivationState.ACTIVE;

    @Column(name = "last_activated_at")
    private Instant lastActivatedAt;

    @Column(name = "last_deactivated_at")
    private Instant lastDeactivatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public AgentActivationState getState() {
        return state;
    }

    public void setState(AgentActivationState state) {
        this.state = state;
    }

    public Instant getLastActivatedAt() {
        return lastActivatedAt;
    }

    public void setLastActivatedAt(Instant lastActivatedAt) {
        this.lastActivatedAt = lastActivatedAt;
    }

    public Instant getLastDeactivatedAt() {
        return lastDeactivatedAt;
    }

    public void setLastDeactivatedAt(Instant lastDeactivatedAt) {
        this.lastDeactivatedAt = lastDeactivatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
