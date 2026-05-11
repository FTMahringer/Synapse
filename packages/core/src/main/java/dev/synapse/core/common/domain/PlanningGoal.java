package dev.synapse.core.common.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "planning_goals")
public class PlanningGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "team_id", nullable = false)
    private String teamId;

    @Column(name = "collaboration_session_id")
    private UUID collaborationSessionId;

    @Column(nullable = false)
    private String title;

    @Column(name = "goal_statement", nullable = false, columnDefinition = "TEXT")
    private String goalStatement;

    @Column(name = "created_by_agent_id", nullable = false)
    private String createdByAgentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status = GoalStatus.ACTIVE;

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

    public enum GoalStatus {
        ACTIVE, COMPLETED, ABANDONED
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public UUID getCollaborationSessionId() {
        return collaborationSessionId;
    }

    public void setCollaborationSessionId(UUID collaborationSessionId) {
        this.collaborationSessionId = collaborationSessionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGoalStatement() {
        return goalStatement;
    }

    public void setGoalStatement(String goalStatement) {
        this.goalStatement = goalStatement;
    }

    public String getCreatedByAgentId() {
        return createdByAgentId;
    }

    public void setCreatedByAgentId(String createdByAgentId) {
        this.createdByAgentId = createdByAgentId;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
