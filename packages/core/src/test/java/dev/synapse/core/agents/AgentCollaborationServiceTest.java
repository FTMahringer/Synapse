package dev.synapse.core.agents;

import dev.synapse.agents.service.AgentCollaborationService;
import dev.synapse.core.common.domain.CollaborationSession;
import dev.synapse.core.common.domain.CollaborationSharedContextEntry;
import dev.synapse.core.common.domain.TeamMembership;
import dev.synapse.core.common.repository.AgentTeamRepository;
import dev.synapse.core.common.repository.CollaborationDelegationRepository;
import dev.synapse.core.common.repository.CollaborationMessageRepository;
import dev.synapse.core.common.repository.CollaborationSessionRepository;
import dev.synapse.core.common.repository.CollaborationSharedContextRepository;
import dev.synapse.core.common.repository.TaskRepository;
import dev.synapse.core.common.repository.TeamMembershipRepository;
import dev.synapse.core.infrastructure.exception.ValidationException;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentCollaborationServiceTest {

    @Mock
    private AgentTeamRepository teamRepository;
    @Mock
    private TeamMembershipRepository teamMembershipRepository;
    @Mock
    private CollaborationSessionRepository sessionRepository;
    @Mock
    private CollaborationMessageRepository messageRepository;
    @Mock
    private CollaborationDelegationRepository delegationRepository;
    @Mock
    private CollaborationSharedContextRepository sharedContextRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private SystemLogService logService;

    private AgentCollaborationService collaborationService;

    @BeforeEach
    void setUp() {
        collaborationService = new AgentCollaborationService(
            teamRepository,
            teamMembershipRepository,
            sessionRepository,
            messageRepository,
            delegationRepository,
            sharedContextRepository,
            taskRepository,
            logService
        );
    }

    @Test
    void createSession_rejectsWhenInitiatorNotInTeam() {
        when(teamRepository.existsById("ops-team")).thenReturn(true);
        when(teamMembershipRepository.findByTeamId("ops-team")).thenReturn(List.of());

        assertThrows(
            ValidationException.class,
            () -> collaborationService.createSession("ops-team", "agent-alpha", null)
        );
    }

    @Test
    void upsertSharedContext_incrementsVersionForExistingKey() {
        UUID sessionId = UUID.randomUUID();
        CollaborationSession session = new CollaborationSession();
        session.setId(sessionId);
        session.setTeamId("ops-team");
        session.setStatus(CollaborationSession.SessionStatus.ACTIVE);

        TeamMembership membership = new TeamMembership();
        membership.setTeamId("ops-team");
        membership.setAgentId("agent-alpha");

        CollaborationSharedContextEntry existing = new CollaborationSharedContextEntry();
        existing.setId(UUID.randomUUID());
        existing.setSessionId(sessionId);
        existing.setContextKey("project.state");
        existing.setVersion(2);

        when(teamRepository.existsById("ops-team")).thenReturn(true);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(teamMembershipRepository.findByTeamId("ops-team")).thenReturn(List.of(membership));
        when(sharedContextRepository.findBySessionIdAndContextKey(sessionId, "project.state"))
            .thenReturn(Optional.of(existing));
        when(sharedContextRepository.save(any(CollaborationSharedContextEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CollaborationSharedContextEntry updated = collaborationService.upsertSharedContext(
            "ops-team",
            sessionId,
            "project.state",
            "in-progress",
            "agent-alpha"
        );

        assertEquals(3, updated.getVersion());
        assertEquals("agent-alpha", updated.getUpdatedByAgentId());
    }
}
