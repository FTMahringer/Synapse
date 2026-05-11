package dev.synapse.agents;

import dev.synapse.agents.service.AgentCollaborationService;
import dev.synapse.core.common.domain.CollaborationMessage;
import dev.synapse.core.dto.CollaborationDelegationDTO;
import dev.synapse.core.dto.CollaborationMessageDTO;
import dev.synapse.core.dto.CollaborationSessionDTO;
import dev.synapse.core.dto.CreateCollaborationSessionRequest;
import dev.synapse.core.dto.CreateDelegationRequest;
import dev.synapse.core.dto.DtoMapper;
import dev.synapse.core.dto.SendCollaborationMessageRequest;
import dev.synapse.core.dto.SharedContextEntryDTO;
import dev.synapse.core.dto.UpsertSharedContextRequest;
import dev.synapse.core.infrastructure.exception.ValidationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/collaboration")
public class AgentCollaborationController {

    private final AgentCollaborationService collaborationService;

    public AgentCollaborationController(AgentCollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    @GetMapping("/sessions")
    public List<CollaborationSessionDTO> listSessions(@PathVariable String teamId) {
        return collaborationService.listSessions(teamId).stream().map(DtoMapper::toDTO).toList();
    }

    @GetMapping("/sessions/{sessionId}")
    public CollaborationSessionDTO getSession(@PathVariable String teamId, @PathVariable UUID sessionId) {
        return DtoMapper.toDTO(collaborationService.getSession(teamId, sessionId));
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public CollaborationSessionDTO createSession(
        @PathVariable String teamId,
        @Valid @RequestBody CreateCollaborationSessionRequest request
    ) {
        return DtoMapper.toDTO(
            collaborationService.createSession(teamId, request.initiatedByAgentId(), request.conversationId())
        );
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public List<CollaborationMessageDTO> listMessages(@PathVariable String teamId, @PathVariable UUID sessionId) {
        return collaborationService.listMessages(teamId, sessionId).stream().map(DtoMapper::toDTO).toList();
    }

    @PostMapping("/sessions/{sessionId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public CollaborationMessageDTO sendMessage(
        @PathVariable String teamId,
        @PathVariable UUID sessionId,
        @Valid @RequestBody SendCollaborationMessageRequest request
    ) {
        return DtoMapper.toDTO(
            collaborationService.sendMessage(
                teamId,
                sessionId,
                request.fromAgentId(),
                request.toAgentId(),
                parseMessageType(request.messageType()),
                request.content(),
                request.metadata()
            )
        );
    }

    @GetMapping("/sessions/{sessionId}/delegations")
    public List<CollaborationDelegationDTO> listDelegations(@PathVariable String teamId, @PathVariable UUID sessionId) {
        return collaborationService.listDelegations(teamId, sessionId).stream().map(DtoMapper::toDTO).toList();
    }

    @PostMapping("/sessions/{sessionId}/delegations")
    @ResponseStatus(HttpStatus.CREATED)
    public CollaborationDelegationDTO createDelegation(
        @PathVariable String teamId,
        @PathVariable UUID sessionId,
        @Valid @RequestBody CreateDelegationRequest request
    ) {
        return DtoMapper.toDTO(
            collaborationService.delegateTask(
                teamId,
                sessionId,
                request.taskId(),
                request.fromAgentId(),
                request.toAgentId(),
                request.delegationNote()
            )
        );
    }

    @GetMapping("/sessions/{sessionId}/context")
    public List<SharedContextEntryDTO> listSharedContext(@PathVariable String teamId, @PathVariable UUID sessionId) {
        return collaborationService.listSharedContext(teamId, sessionId).stream().map(DtoMapper::toDTO).toList();
    }

    @PutMapping("/sessions/{sessionId}/context/{contextKey}")
    public SharedContextEntryDTO upsertSharedContext(
        @PathVariable String teamId,
        @PathVariable UUID sessionId,
        @PathVariable String contextKey,
        @Valid @RequestBody UpsertSharedContextRequest request
    ) {
        return DtoMapper.toDTO(
            collaborationService.upsertSharedContext(
                teamId,
                sessionId,
                contextKey,
                request.value(),
                request.updatedByAgentId()
            )
        );
    }

    private CollaborationMessage.MessageType parseMessageType(String messageType) {
        try {
            return CollaborationMessage.MessageType.valueOf(messageType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown collaboration message type: " + messageType);
        }
    }
}
