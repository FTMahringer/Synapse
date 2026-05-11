package dev.synapse.core.common.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
    name = "planning_artifacts",
    uniqueConstraints = @UniqueConstraint(columnNames = {"goal_id", "plan_version"})
)
public class PlanningArtifact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "goal_id", nullable = false)
    private UUID goalId;

    @Column(name = "plan_version", nullable = false)
    private Integer planVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatus status = PlanStatus.ACTIVE;

    @Column(name = "compact_summary", nullable = false, columnDefinition = "TEXT")
    private String compactSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps_json", nullable = false, columnDefinition = "jsonb")
    private List<Map<String, Object>> stepsJson = List.of();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reasoning_chain_json", nullable = false, columnDefinition = "jsonb")
    private List<Map<String, Object>> reasoningChainJson = List.of();

    @Column(name = "total_steps", nullable = false)
    private Integer totalSteps = 0;

    @Column(name = "completed_steps", nullable = false)
    private Integer completedSteps = 0;

    @Column(name = "created_by_agent_id", nullable = false)
    private String createdByAgentId;

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

    public enum PlanStatus {
        DRAFT, ACTIVE, SUPERSEDED
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getGoalId() {
        return goalId;
    }

    public void setGoalId(UUID goalId) {
        this.goalId = goalId;
    }

    public Integer getPlanVersion() {
        return planVersion;
    }

    public void setPlanVersion(Integer planVersion) {
        this.planVersion = planVersion;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public String getCompactSummary() {
        return compactSummary;
    }

    public void setCompactSummary(String compactSummary) {
        this.compactSummary = compactSummary;
    }

    public List<Map<String, Object>> getStepsJson() {
        return stepsJson;
    }

    public void setStepsJson(List<Map<String, Object>> stepsJson) {
        this.stepsJson = stepsJson;
    }

    public List<Map<String, Object>> getReasoningChainJson() {
        return reasoningChainJson;
    }

    public void setReasoningChainJson(List<Map<String, Object>> reasoningChainJson) {
        this.reasoningChainJson = reasoningChainJson;
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public Integer getCompletedSteps() {
        return completedSteps;
    }

    public void setCompletedSteps(Integer completedSteps) {
        this.completedSteps = completedSteps;
    }

    public String getCreatedByAgentId() {
        return createdByAgentId;
    }

    public void setCreatedByAgentId(String createdByAgentId) {
        this.createdByAgentId = createdByAgentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
