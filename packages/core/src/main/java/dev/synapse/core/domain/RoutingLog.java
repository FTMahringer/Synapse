package dev.synapse.core.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Log of routing decisions made by Main Agent.
 * Tracks what requests were routed where and why.
 */
@Entity
@Table(name = "routing_logs")
public class RoutingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoutingDecision decision;

    @Column(name = "target_agent_id")
    private String targetAgentId;

    @Column(name = "target_team_id")
    private UUID targetTeamId;

    @Column(name = "target_project_id")
    private UUID targetProjectId;

    @Column(columnDefinition = "TEXT")
    private String reasoning;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public RoutingDecision getDecision() {
        return decision;
    }

    public void setDecision(RoutingDecision decision) {
        this.decision = decision;
    }

    public String getTargetAgentId() {
        return targetAgentId;
    }

    public void setTargetAgentId(String targetAgentId) {
        this.targetAgentId = targetAgentId;
    }

    public UUID getTargetTeamId() {
        return targetTeamId;
    }

    public void setTargetTeamId(UUID targetTeamId) {
        this.targetTeamId = targetTeamId;
    }

    public UUID getTargetProjectId() {
        return targetProjectId;
    }

    public void setTargetProjectId(UUID targetProjectId) {
        this.targetProjectId = targetProjectId;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
