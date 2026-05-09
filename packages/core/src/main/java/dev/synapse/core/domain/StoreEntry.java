package dev.synapse.core.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * A plugin or bundle entry from the store registry.
 */
@Entity
@Table(name = "store_entries")
public class StoreEntry {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreEntryType type;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String version;

    @Column
    private String author;

    @Column
    private String license;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "min_synapse")
    private String minSynapse;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> meta = Map.of();

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    @PrePersist
    @PreUpdate
    protected void onSync() {
        syncedAt = Instant.now();
    }

    public enum StoreEntryType {
        PLUGIN, BUNDLE
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public StoreEntryType getType() { return type; }
    public void setType(StoreEntryType type) { this.type = type; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMinSynapse() { return minSynapse; }
    public void setMinSynapse(String minSynapse) { this.minSynapse = minSynapse; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Map<String, Object> getMeta() { return meta; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }

    public Instant getSyncedAt() { return syncedAt; }
}
