package dev.synapse.core.agents.service;

import dev.synapse.core.common.domain.AgentHeartbeat;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.common.repository.AgentHeartbeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Records and queries heartbeat pulses from active agents.
 * An agent is considered alive if it pulsed within the liveness window.
 */
@Service
public class AgentHeartbeatService {

    private static final Duration LIVENESS_WINDOW = Duration.ofMinutes(5);

    private final AgentHeartbeatRepository heartbeatRepository;
    private final SystemLogService logService;

    public AgentHeartbeatService(
        AgentHeartbeatRepository heartbeatRepository,
        SystemLogService logService
    ) {
        this.heartbeatRepository = heartbeatRepository;
        this.logService = logService;
    }

    /**
     * Record a heartbeat pulse for an agent.
     */
    @Transactional
    public AgentHeartbeat pulse(String agentId, String note) {
        AgentHeartbeat heartbeat = new AgentHeartbeat();
        heartbeat.setAgentId(agentId);
        heartbeat.setNote(note);

        AgentHeartbeat saved = heartbeatRepository.save(heartbeat);

        logService.log(
            LogLevel.DEBUG,
            LogCategory.HEARTBEAT,
            Map.of("component", "AgentHeartbeatService", "agentId", agentId),
            "AGENT_HEARTBEAT",
            Map.of("agentId", agentId),
            null,
            null
        );

        return saved;
    }

    /**
     * Get the most recent heartbeat for an agent.
     */
    @Transactional(readOnly = true)
    public Optional<AgentHeartbeat> getLatest(String agentId) {
        return heartbeatRepository.findTopByAgentIdOrderByRecordedAtDesc(agentId);
    }

    /**
     * Get heartbeat history for an agent.
     */
    @Transactional(readOnly = true)
    public List<AgentHeartbeat> getHistory(String agentId) {
        return heartbeatRepository.findByAgentIdOrderByRecordedAtDesc(agentId);
    }

    /**
     * Check if an agent is alive (pulsed within the liveness window).
     */
    @Transactional(readOnly = true)
    public boolean isAlive(String agentId) {
        return heartbeatRepository.findTopByAgentIdOrderByRecordedAtDesc(agentId)
            .map(h -> h.getRecordedAt().isAfter(Instant.now().minus(LIVENESS_WINDOW)))
            .orElse(false);
    }

    /**
     * Get all agent IDs that have pulsed within the liveness window.
     */
    @Transactional(readOnly = true)
    public List<String> getAliveAgentIds() {
        return heartbeatRepository.findActiveAgentIdsSince(Instant.now().minus(LIVENESS_WINDOW));
    }
}
