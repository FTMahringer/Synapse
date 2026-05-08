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
        log.setReasoning("Main Agent handles all requests directly");

        routingLogRepository.save(log);

        return new RoutingResult(
            RoutingDecision.HANDLE_DIRECTLY,
            "main-agent",
            null,
            null,
            "Main Agent handles all requests directly"
        );
    }

    /**
     * Route a message to the AI-Firm project dispatch entry point.
     */
    @Transactional
    public RoutingResult routeToFirm(UUID conversationId, UUID messageId, UUID projectId, String reasoning) {
        RoutingLog log = new RoutingLog();
        log.setConversationId(conversationId);
        log.setMessageId(messageId);
        log.setDecision(RoutingDecision.ROUTE_TO_FIRM_PROJECT);
        log.setTargetProjectId(projectId);
        log.setReasoning(reasoning);

        routingLogRepository.save(log);

        return new RoutingResult(
            RoutingDecision.ROUTE_TO_FIRM_PROJECT,
            null,
            null,
            projectId,
            reasoning
        );
    }

    /**
     * Get routing history for a conversation.
     */
    public List<RoutingLog> getRoutingHistory(UUID conversationId) {
        return routingLogRepository.findByConversationIdOrderByCreatedAtDesc(conversationId);
    }

    /**
     * Get all routing logs, most recent first.
     */
    public List<RoutingLog> getAllRoutingLogs() {
        return routingLogRepository.findAll()
            .stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .toList();
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
