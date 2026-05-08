package dev.synapse.core.service;

import dev.synapse.core.domain.RoutingDecision;
import dev.synapse.core.domain.RoutingLog;
import dev.synapse.core.repository.RoutingLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Main Agent router service.
 * Makes routing decisions and logs them for inspection.
 * 
 * For now, implements simple deterministic rules:
 * - All requests handled directly by Main Agent
 * - Routing logic to be expanded in future steps
 */
@Service
public class MainAgentRouterService {

    private final RoutingLogRepository routingLogRepository;
    private final AgentRuntimeService agentRuntimeService;

    public MainAgentRouterService(
        RoutingLogRepository routingLogRepository,
        AgentRuntimeService agentRuntimeService
    ) {
        this.routingLogRepository = routingLogRepository;
        this.agentRuntimeService = agentRuntimeService;
    }

    /**
     * Route a message and log the decision.
     * 
     * @param conversationId Conversation ID
     * @param messageId Message ID
     * @param messageContent Message content
     * @return Routing decision with target info
     */
    @Transactional
    public RoutingResult route(UUID conversationId, UUID messageId, String messageContent) {
        // For now: always handle directly
        // Future: parse intent, check agent capabilities, route to teams/firms
        
        RoutingLog log = new RoutingLog();
        log.setConversationId(conversationId);
        log.setMessageId(messageId);
        log.setDecision(RoutingDecision.HANDLE_DIRECTLY);
        log.setReasoning("Main Agent handles all requests in v1.4.2");
        
        routingLogRepository.save(log);
        
        return new RoutingResult(
            RoutingDecision.HANDLE_DIRECTLY,
            "main-agent",
            null,
            null,
            "Main Agent handles all requests in v1.4.2"
        );
    }

    /**
     * Get routing history for a conversation.
     */
    public List<RoutingLog> getRoutingHistory(UUID conversationId) {
        return routingLogRepository.findByConversationIdOrderByCreatedAtDesc(conversationId);
    }

    /**
     * Result of a routing decision.
     */
    public record RoutingResult(
        RoutingDecision decision,
        String targetAgentId,
        UUID targetTeamId,
        UUID targetProjectId,
        String reasoning
    ) {}
}
