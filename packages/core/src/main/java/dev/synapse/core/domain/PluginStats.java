package dev.synapse.core.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Usage stats for an installed plugin.
 */
@Entity
@Table(name = "plugin_stats")
public class PluginStats {

    @Id
    @Column(name = "plugin_id")
    private String pluginId;

    @Column(name = "install_count", nullable = false)
    private long installCount = 0;

    @Column(name = "enable_count", nullable = false)
    private long enableCount = 0;

    @Column(name = "disable_count", nullable = false)
    private long disableCount = 0;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getPluginId() { return pluginId; }
    public void setPluginId(String pluginId) { this.pluginId = pluginId; }

    public long getInstallCount() { return installCount; }
    public void setInstallCount(long installCount) { this.installCount = installCount; }

    public long getEnableCount() { return enableCount; }
    public void setEnableCount(long enableCount) { this.enableCount = enableCount; }

    public long getDisableCount() { return disableCount; }
    public void setDisableCount(long disableCount) { this.disableCount = disableCount; }

    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}
