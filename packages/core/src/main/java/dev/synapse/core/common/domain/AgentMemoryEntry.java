package dev.synapse.core.common.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemoryTier tier = MemoryTier.SHORT_TERM;

    @Column(name = "promoted_at")
    private Instant promotedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_reason")
    private PromotionReason promotionReason;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    @Column(name = "access_count", nullable = false)
    private Integer accessCount = 0;

    @Column(name = "retention_until")
    private Instant retentionUntil;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_entry_ids", columnDefinition = "jsonb")
    private List<String> sourceEntryIds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
        if (lastAccessedAt == null) {
            lastAccessedAt = createdAt;
        }
        if (accessCount == null) {
            accessCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum MemoryTier {
        SHORT_TERM, KNOWLEDGE, ARCHIVE
    }

    public enum PromotionReason {
        REUSED, PINNED, SUMMARIZED, CONSOLIDATED, MANUAL
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

    public MemoryTier getTier() { return tier; }
    public void setTier(MemoryTier tier) { this.tier = tier; }

    public Instant getPromotedAt() { return promotedAt; }
    public void setPromotedAt(Instant promotedAt) { this.promotedAt = promotedAt; }

    public PromotionReason getPromotionReason() { return promotionReason; }
    public void setPromotionReason(PromotionReason promotionReason) { this.promotionReason = promotionReason; }

    public Instant getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(Instant lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }

    public Integer getAccessCount() { return accessCount; }
    public void setAccessCount(Integer accessCount) { this.accessCount = accessCount; }

    public Instant getRetentionUntil() { return retentionUntil; }
    public void setRetentionUntil(Instant retentionUntil) { this.retentionUntil = retentionUntil; }

    public List<String> getSourceEntryIds() { return sourceEntryIds; }
    public void setSourceEntryIds(List<String> sourceEntryIds) { this.sourceEntryIds = sourceEntryIds; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
