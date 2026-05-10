package dev.synapse.core.agents.service;

import dev.synapse.core.agents.AgentDefinition;
import dev.synapse.core.agents.AgentDefinitionLoader;
import dev.synapse.core.common.domain.Agent;
import dev.synapse.core.common.domain.Agent.AgentType;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.common.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AgentManagementService {

    private final AgentRepository agentRepository;
    private final AgentDefinitionLoader agentDefinitionLoader;
    private final SystemLogService logService;

    public AgentManagementService(
        AgentRepository agentRepository,
        AgentDefinitionLoader agentDefinitionLoader,
        SystemLogService logService
    ) {
        this.agentRepository = agentRepository;
        this.agentDefinitionLoader = agentDefinitionLoader;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public List<Agent> listAllAgents() {
        List<Agent> dbAgents = agentRepository.findAll();
        List<AgentDefinition> fileAgents = agentDefinitionLoader.listAgents();
        
        Map<String, Agent> agentMap = dbAgents.stream()
            .collect(Collectors.toMap(Agent::getId, a -> a));
        
        for (AgentDefinition def : fileAgents) {
            if (!agentMap.containsKey(def.id())) {
                Agent agent = fromDefinition(def);
                agentMap.put(def.id(), agent);
            }
        }
        
        return new ArrayList<>(agentMap.values());
    }

    @Transactional(readOnly = true)
    public Agent findById(String id) {
        Optional<Agent> dbAgent = agentRepository.findById(id);
        if (dbAgent.isPresent()) {
            return dbAgent.get();
        }
        
        return agentDefinitionLoader.listAgents().stream()
            .filter(def -> def.id().equals(id))
            .map(this::fromDefinition)
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Agent", id));
    }

    @Transactional
    public Agent createAgent(Agent agent) {
        Agent saved = agentRepository.save(agent);
        
        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT,
            Map.of("component", "AgentManagementService", "agentId", agent.getId()),
            "AGENT_CREATED",
            Map.of("name", agent.getName(), "type", agent.getType().name()),
            null,
            null
        );
        
        return saved;
    }

    @Transactional
    public Agent updateAgent(String id, Agent updates) {
        Agent existing = agentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Agent", id));
        
        if (updates.getName() != null) {
            existing.setName(updates.getName());
        }
        if (updates.getType() != null) {
            existing.setType(updates.getType());
        }
        if (updates.getStatus() != null) {
            existing.setStatus(updates.getStatus());
        }
        if (updates.getConfig() != null) {
            existing.setConfig(updates.getConfig());
        }
        
        Agent saved = agentRepository.save(existing);
        
        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT,
            Map.of("component", "AgentManagementService", "agentId", id),
            "AGENT_UPDATED",
            Map.of("name", saved.getName()),
            null,
            null
        );
        
        return saved;
    }

    @Transactional
    public void deleteAgent(String id) {
        if (!agentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agent", id);
        }
        
        agentRepository.deleteById(id);
        
        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT,
            Map.of("component", "AgentManagementService", "agentId", id),
            "AGENT_DELETED",
            Map.of(),
            null,
            null
        );
    }

    @Transactional
    public void syncFileAgentsToDatabase() {
        List<AgentDefinition> fileAgents = agentDefinitionLoader.listAgents();
        List<Agent> existingAgents = agentRepository.findAll();
        
        Set<String> existingIds = existingAgents.stream()
            .map(Agent::getId)
            .collect(Collectors.toSet());
        
        int synced = 0;
        for (AgentDefinition def : fileAgents) {
            if (!existingIds.contains(def.id())) {
                Agent agent = fromDefinition(def);
                agentRepository.save(agent);
                synced++;
            }
        }
        
        if (synced > 0) {
            logService.log(
                LogLevel.INFO,
                LogCategory.AGENT,
                Map.of("component", "AgentManagementService"),
                "AGENTS_SYNCED",
                Map.of("count", synced),
                null,
                null
            );
        }
    }

    private Agent fromDefinition(AgentDefinition def) {
        Agent agent = new Agent();
        agent.setId(def.id());
        agent.setName(def.name());
        agent.setType(parseAgentType(def.type()));
        agent.setStatus(Agent.AgentStatus.OFFLINE);
        agent.setConfig(Map.of(
            "path", def.path(),
            "files", def.files(),
            "source", "file"
        ));
        return agent;
    }

    private AgentType parseAgentType(String type) {
        return switch (type.toLowerCase()) {
            case "main" -> AgentType.MAIN;
            case "team-member" -> AgentType.TEAM_MEMBER;
            case "team-leader" -> AgentType.TEAM_LEADER;
            case "firm-ceo" -> AgentType.FIRM_CEO;
            case "firm-agent" -> AgentType.FIRM_AGENT;
            default -> AgentType.CUSTOM;
        };
    }
}
