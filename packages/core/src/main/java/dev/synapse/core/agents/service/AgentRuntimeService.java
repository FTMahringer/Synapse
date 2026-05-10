package dev.synapse.core.agents.service;

import dev.synapse.core.domain.AgentActivationState;
import dev.synapse.core.domain.AgentRuntimeRegistry;
import dev.synapse.core.repository.AgentRuntimeRegistryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Manages agent runtime activation states.
 * Tracks which agents are active, paused, or disabled.
 */
@Service
public class AgentRuntimeService {

    private final AgentRuntimeRegistryRepository repository;
    private final AgentHeartbeatService heartbeatService;

    public AgentRuntimeService(
        AgentRuntimeRegistryRepository repository,
        AgentHeartbeatService heartbeatService
    ) {
        this.repository = repository;
        this.heartbeatService = heartbeatService;
    }

    /**
     * Get runtime state for an agent, creating entry if not exists.
     */
    @Transactional
    public AgentRuntimeRegistry getOrCreateRuntime(String agentId) {
        return repository.findByAgentId(agentId)
            .orElseGet(() -> {
                AgentRuntimeRegistry runtime = new AgentRuntimeRegistry();
                runtime.setAgentId(agentId);
                runtime.setState(AgentActivationState.ACTIVE);
                runtime.setLastActivatedAt(Instant.now());
                return repository.save(runtime);
            });
    }

    /**
     * Activate an agent and record an initial heartbeat.
     */
    @Transactional
    public AgentRuntimeRegistry activate(String agentId) {
        AgentRuntimeRegistry runtime = getOrCreateRuntime(agentId);
        runtime.setState(AgentActivationState.ACTIVE);
        runtime.setLastActivatedAt(Instant.now());
        AgentRuntimeRegistry saved = repository.save(runtime);
        heartbeatService.pulse(agentId, "activated");
        return saved;
    }

    /**
     * Pause an agent (temporarily inactive).
     */
    @Transactional
    public AgentRuntimeRegistry pause(String agentId) {
        AgentRuntimeRegistry runtime = getOrCreateRuntime(agentId);
        runtime.setState(AgentActivationState.PAUSED);
        runtime.setLastDeactivatedAt(Instant.now());
        return repository.save(runtime);
    }

    /**
     * Disable an agent (permanently inactive until re-enabled).
     */
    @Transactional
    public AgentRuntimeRegistry disable(String agentId) {
        AgentRuntimeRegistry runtime = getOrCreateRuntime(agentId);
        runtime.setState(AgentActivationState.DISABLED);
        runtime.setLastDeactivatedAt(Instant.now());
        return repository.save(runtime);
    }

    /**
     * Check if an agent is currently active.
     */
    public boolean isActive(String agentId) {
        Optional<AgentRuntimeRegistry> runtime = repository.findByAgentId(agentId);
        return runtime.map(r -> r.getState() == AgentActivationState.ACTIVE).orElse(false);
    }

    /**
     * Get all agent runtimes.
     */
    public List<AgentRuntimeRegistry> findAll() {
        return repository.findAll();
    }

    /**
     * Get runtime state for specific agent.
     */
    public Optional<AgentRuntimeRegistry> findByAgentId(String agentId) {
        return repository.findByAgentId(agentId);
    }
}
