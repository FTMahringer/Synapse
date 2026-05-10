package dev.synapse.core.common.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * A key-value memory entry for an agent's persistent vault.
 * Agents read and write memory through this table.
 */
@Entity
@Table(
    name = "agent_memory_entries",
    uniqueConstraints = @UniqueConstraint(columnNames = {"agent_id", "memory_key"})
)
public class AgentMemoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(name = "memory_key", nullable = false)
    private String key;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String value;

    @Column
    private String namespace;

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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
