package dev.synapse.core.common.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "plugins")
public class Plugin {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PluginType type;

    @Column(nullable = false)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PluginStatus status = PluginStatus.INSTALLED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> manifest = Map.of();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_tier", nullable = false)
    private StorageTier storageTier = StorageTier.SYSTEM;

    @Enumerated(EnumType.STRING)
    @Column(name = "loader_state", nullable = false)
    private LoaderState loaderState = LoaderState.UNLOADED;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "loaded_at")
    private Instant loadedAt;

    @Column(name = "api_version")
    private String apiVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "trust_tier", nullable = false)
    private TrustTier trustTier = TrustTier.COMMUNITY;

    @Transient
    private List<String> dependencies = new ArrayList<>();

    @Column(name = "scan_clean")
    private Boolean scanClean;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scan_violations", columnDefinition = "jsonb")
    private Map<String, Object> scanViolations;

    @Column(name = "sandbox_enabled", nullable = false)
    private boolean sandboxEnabled = true;

    @Column(name = "lifecycle_timeout_ms")
    private Long lifecycleTimeoutMs;

    @Column(name = "message_timeout_ms")
    private Long messageTimeoutMs;

    @Column(name = "max_logs_per_minute")
    private Integer maxLogsPerMinute;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public enum PluginType {
        CHANNEL,
        MODEL,
        SKILL,
        MCP,
    }

    public enum PluginStatus {
        INSTALLED,
        DISABLED,
        ERROR,
    }

    public enum StorageTier {
        SYSTEM,
        STAGING,
    }

    public enum LoaderState {
        UNLOADED,
        LOADING,
        LOADED,
        ERROR,
    }

    public enum TrustTier {
        OFFICIAL,
        COMMUNITY,
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PluginType getType() {
        return type;
    }

    public void setType(PluginType type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public PluginStatus getStatus() {
        return status;
    }

    public void setStatus(PluginStatus status) {
        this.status = status;
    }

    public Map<String, Object> getManifest() {
        return manifest;
    }

    public void setManifest(Map<String, Object> manifest) {
        this.manifest = manifest;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public StorageTier getStorageTier() {
        return storageTier;
    }

    public void setStorageTier(StorageTier storageTier) {
        this.storageTier = storageTier;
    }

    public LoaderState getLoaderState() {
        return loaderState;
    }

    public void setLoaderState(LoaderState loaderState) {
        this.loaderState = loaderState;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getLoadedAt() {
        return loadedAt;
    }

    public void setLoadedAt(Instant loadedAt) {
        this.loadedAt = loadedAt;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public TrustTier getTrustTier() {
        return trustTier;
    }

    public void setTrustTier(TrustTier trustTier) {
        this.trustTier = trustTier;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies =
            dependencies != null ? dependencies : new ArrayList<>();
    }

    public Boolean getScanClean() {
        return scanClean;
    }

    public void setScanClean(Boolean scanClean) {
        this.scanClean = scanClean;
    }

    public Map<String, Object> getScanViolations() {
        return scanViolations;
    }

    public void setScanViolations(Map<String, Object> scanViolations) {
        this.scanViolations = scanViolations;
    }

    public boolean isSandboxEnabled() {
        return sandboxEnabled;
    }

    public void setSandboxEnabled(boolean sandboxEnabled) {
        this.sandboxEnabled = sandboxEnabled;
    }

    public Long getLifecycleTimeoutMs() {
        return lifecycleTimeoutMs;
    }

    public void setLifecycleTimeoutMs(Long lifecycleTimeoutMs) {
        this.lifecycleTimeoutMs = lifecycleTimeoutMs;
    }

    public Long getMessageTimeoutMs() {
        return messageTimeoutMs;
    }

    public void setMessageTimeoutMs(Long messageTimeoutMs) {
        this.messageTimeoutMs = messageTimeoutMs;
    }

    public Integer getMaxLogsPerMinute() {
        return maxLogsPerMinute;
    }

    public void setMaxLogsPerMinute(Integer maxLogsPerMinute) {
        this.maxLogsPerMinute = maxLogsPerMinute;
    }
}
