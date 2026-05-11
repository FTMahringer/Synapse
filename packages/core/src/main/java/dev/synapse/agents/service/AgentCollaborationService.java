package dev.synapse.agents.service;

import dev.synapse.core.common.domain.CollaborationDelegation;
import dev.synapse.core.common.domain.CollaborationMessage;
import dev.synapse.core.common.domain.CollaborationSession;
import dev.synapse.core.common.domain.CollaborationSharedContextEntry;
import dev.synapse.core.common.repository.AgentTeamRepository;
import dev.synapse.core.common.repository.CollaborationDelegationRepository;
import dev.synapse.core.common.repository.CollaborationMessageRepository;
import dev.synapse.core.common.repository.CollaborationSessionRepository;
import dev.synapse.core.common.repository.CollaborationSharedContextRepository;
import dev.synapse.core.common.repository.TaskRepository;
import dev.synapse.core.common.repository.TeamMembershipRepository;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.exception.ValidationException;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AgentCollaborationService {

    private final AgentTeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final CollaborationSessionRepository sessionRepository;
    private final CollaborationMessageRepository messageRepository;
    private final CollaborationDelegationRepository delegationRepository;
    private final CollaborationSharedContextRepository sharedContextRepository;
    private final TaskRepository taskRepository;
    private final SystemLogService logService;

    public AgentCollaborationService(
        AgentTeamRepository teamRepository,
        TeamMembershipRepository teamMembershipRepository,
        CollaborationSessionRepository sessionRepository,
        CollaborationMessageRepository messageRepository,
        CollaborationDelegationRepository delegationRepository,
        CollaborationSharedContextRepository sharedContextRepository,
        TaskRepository taskRepository,
        SystemLogService logService
    ) {
        this.teamRepository = teamRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.delegationRepository = delegationRepository;
        this.sharedContextRepository = sharedContextRepository;
        this.taskRepository = taskRepository;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public List<CollaborationSession> listSessions(String teamId) {
        ensureTeamExists(teamId);
        return sessionRepository.findByTeamIdOrderByCreatedAtDesc(teamId);
    }

    @Transactional(readOnly = true)
    public CollaborationSession getSession(String teamId, UUID sessionId) {
        CollaborationSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("CollaborationSession", sessionId.toString()));
        ensureSessionTeam(teamId, session);
        return session;
    }

    @Transactional
    public CollaborationSession createSession(String teamId, String initiatedByAgentId, UUID conversationId) {
        ensureTeamExists(teamId);
        ensureAgentInTeam(teamId, initiatedByAgentId);

        CollaborationSession session = new CollaborationSession();
        session.setTeamId(teamId);
        session.setInitiatedByAgentId(initiatedByAgentId);
        session.setConversationId(conversationId);
        session.setStatus(CollaborationSession.SessionStatus.ACTIVE);

        CollaborationSession saved = sessionRepository.save(session);
        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT_TEAM,
            Map.of("component", "AgentCollaborationService", "sessionId", saved.getId().toString(), "teamId", teamId),
            "COLLABORATION_SESSION_CREATED",
            Map.of("initiatedByAgentId", initiatedByAgentId),
            null,
            null
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CollaborationMessage> listMessages(String teamId, UUID sessionId) {
        CollaborationSession session = getSession(teamId, sessionId);
        ensureSessionActive(session);
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public CollaborationMessage sendMessage(
        String teamId,
        UUID sessionId,
        String fromAgentId,
        String toAgentId,
        CollaborationMessage.MessageType messageType,
        String content,
        Map<String, Object> metadata
    ) {
        CollaborationSession session = getSession(teamId, sessionId);
        ensureSessionActive(session);
        ensureAgentInTeam(teamId, fromAgentId);
        ensureAgentInTeam(teamId, toAgentId);

        CollaborationMessage message = new CollaborationMessage();
        message.setSessionId(sessionId);
        message.setFromAgentId(fromAgentId);
        message.setToAgentId(toAgentId);
        message.setMessageType(messageType);
        message.setContent(content);
        message.setMetadata(metadata != null ? metadata : Map.of());

        CollaborationMessage saved = messageRepository.save(message);
        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT_MESSAGE,
            Map.of("component", "AgentCollaborationService", "sessionId", sessionId.toString()),
            "COLLABORATION_MESSAGE_SENT",
            Map.of("fromAgentId", fromAgentId, "toAgentId", toAgentId, "messageType", messageType.name()),
            null,
            null
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CollaborationDelegation> listDelegations(String teamId, UUID sessionId) {
        CollaborationSession session = getSession(teamId, sessionId);
        ensureSessionActive(session);
        return delegationRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public CollaborationDelegation delegateTask(
        String teamId,
        UUID sessionId,
        UUID taskId,
        String fromAgentId,
        String toAgentId,
        String note
    ) {
        CollaborationSession session = getSession(teamId, sessionId);
        ensureSessionActive(session);
        ensureAgentInTeam(teamId, fromAgentId);
        ensureAgentInTeam(teamId, toAgentId);
        if (taskId != null && !taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task", taskId.toString());
        }

        CollaborationDelegation delegation = new CollaborationDelegation();
        delegation.setSessionId(sessionId);
        delegation.setTaskId(taskId);
        delegation.setFromAgentId(fromAgentId);
        delegation.setToAgentId(toAgentId);
        delegation.setStatus(CollaborationDelegation.DelegationStatus.PENDING);
        delegation.setDelegationNote(note);

        CollaborationDelegation saved = delegationRepository.save(delegation);
        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT_TEAM,
            Map.of("component", "AgentCollaborationService", "sessionId", sessionId.toString()),
            "COLLABORATION_TASK_DELEGATED",
            Map.of("fromAgentId", fromAgentId, "toAgentId", toAgentId, "taskId", taskId != null ? taskId.toString() : ""),
            null,
            null
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CollaborationSharedContextEntry> listSharedContext(String teamId, UUID sessionId) {
        CollaborationSession session = getSession(teamId, sessionId);
        ensureSessionActive(session);
        return sharedContextRepository.findBySessionIdOrderByUpdatedAtDesc(sessionId);
    }

    @Transactional
    public CollaborationSharedContextEntry upsertSharedContext(
        String teamId,
        UUID sessionId,
        String contextKey,
        String contextValue,
        String updatedByAgentId
    ) {
        CollaborationSession session = getSession(teamId, sessionId);
        ensureSessionActive(session);
        ensureAgentInTeam(teamId, updatedByAgentId);

        CollaborationSharedContextEntry entry = sharedContextRepository.findBySessionIdAndContextKey(sessionId, contextKey)
            .orElseGet(() -> {
                CollaborationSharedContextEntry e = new CollaborationSharedContextEntry();
                e.setSessionId(sessionId);
                e.setContextKey(contextKey);
                return e;
            });

        boolean isNew = entry.getId() == null;
        entry.setContextValue(contextValue);
        entry.setUpdatedByAgentId(updatedByAgentId);
        if (isNew) {
            entry.setVersion(1);
        } else {
            int current = entry.getVersion() == null ? 1 : entry.getVersion();
            entry.setVersion(current + 1);
        }

        CollaborationSharedContextEntry saved = sharedContextRepository.save(entry);
        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT_TEAM,
            Map.of("component", "AgentCollaborationService", "sessionId", sessionId.toString()),
            isNew ? "COLLABORATION_CONTEXT_ADDED" : "COLLABORATION_CONTEXT_UPDATED",
            Map.of("contextKey", contextKey, "updatedByAgentId", updatedByAgentId),
            null,
            null
        );
        return saved;
    }

    private void ensureTeamExists(String teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("AgentTeam", teamId);
        }
    }

    private void ensureAgentInTeam(String teamId, String agentId) {
        boolean present = teamMembershipRepository.findByTeamId(teamId).stream()
            .anyMatch(membership -> membership.getAgentId().equals(agentId));
        if (!present) {
            throw new ValidationException("Agent '" + agentId + "' is not a member of team '" + teamId + "'");
        }
    }

    private void ensureSessionTeam(String teamId, CollaborationSession session) {
        if (!session.getTeamId().equals(teamId)) {
            throw new ValidationException("Collaboration session does not belong to team '" + teamId + "'");
        }
    }

    private void ensureSessionActive(CollaborationSession session) {
        if (session.getStatus() != CollaborationSession.SessionStatus.ACTIVE) {
            throw new ValidationException("Collaboration session '" + session.getId() + "' is not active");
        }
    }
}
