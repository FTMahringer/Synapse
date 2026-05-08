package dev.synapse.core.service;

import dev.synapse.core.domain.Agent;
import dev.synapse.core.domain.Agent.AgentType;
import dev.synapse.core.domain.Agent.AgentStatus;
import dev.synapse.core.exception.ResourceNotFoundException;
import dev.synapse.core.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgentService {

    private final AgentRepository agentRepository;

    public AgentService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Transactional(readOnly = true)
    public List<Agent> findAll() {
        return agentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Agent findById(String id) {
        return agentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Agent", id));
    }

    @Transactional(readOnly = true)
    public List<Agent> findByType(AgentType type) {
        return agentRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Agent> findByStatus(AgentStatus status) {
        return agentRepository.findByStatus(status);
    }

    @Transactional
    public Agent save(Agent agent) {
        return agentRepository.save(agent);
    }

    @Transactional
    public void deleteById(String id) {
        if (!agentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agent", id);
        }
        agentRepository.deleteById(id);
    }
}
