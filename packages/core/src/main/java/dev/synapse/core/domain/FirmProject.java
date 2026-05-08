package dev.synapse.core.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * A project dispatched to the AI-Firm by the Main Agent.
 * Singleton firm constraint is enforced at service layer.
 */
@Entity
@Table(name = "firm_projects")
public class FirmProject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "firm_id", nullable = false)
    private String firmId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FirmProjectStatus status = FirmProjectStatus.PENDING;

    @Column(name = "dispatched_by_agent_id", nullable = false)
    private String dispatchedByAgentId;

    @Column(name = "assigned_team_id")
    private String assignedTeamId;

    @Column(name = "conversation_id")
    private UUID conversationId;

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
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

    public enum FirmProjectStatus {
        PENDING, ACCEPTED, IN_PROGRESS, BLOCKED, COMPLETED, REJECTED
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFirmId() { return firmId; }
    public void setFirmId(String firmId) { this.firmId = firmId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public FirmProjectStatus getStatus() { return status; }
    public void setStatus(FirmProjectStatus status) { this.status = status; }

    public String getDispatchedByAgentId() { return dispatchedByAgentId; }
    public void setDispatchedByAgentId(String dispatchedByAgentId) { this.dispatchedByAgentId = dispatchedByAgentId; }

    public String getAssignedTeamId() { return assignedTeamId; }
    public void setAssignedTeamId(String assignedTeamId) { this.assignedTeamId = assignedTeamId; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
