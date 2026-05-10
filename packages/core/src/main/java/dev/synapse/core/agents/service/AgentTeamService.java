package dev.synapse.core.agents.service;

import dev.synapse.core.domain.AgentTeam;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.repository.AgentTeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AgentTeamService {

    private final AgentTeamRepository teamRepository;
    private final SystemLogService logService;

    public AgentTeamService(AgentTeamRepository teamRepository, SystemLogService logService) {
        this.teamRepository = teamRepository;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public List<AgentTeam> findAll() {
        return teamRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AgentTeam findById(String id) {
        return teamRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AgentTeam", id));
    }

    @Transactional
    public AgentTeam save(AgentTeam team) {
        AgentTeam saved = teamRepository.save(team);
        
        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT,
            Map.of("component", "AgentTeamService", "teamId", team.getId()),
            "TEAM_CREATED",
            Map.of("name", team.getName()),
            null,
            null
        );
        
        return saved;
    }

    @Transactional
    public AgentTeam update(String id, AgentTeam updates) {
        AgentTeam existing = findById(id);
        
        if (updates.getName() != null) {
            existing.setName(updates.getName());
        }
        if (updates.getLeaderAgentId() != null) {
            existing.setLeaderAgentId(updates.getLeaderAgentId());
        }
        if (updates.getConfig() != null) {
            existing.setConfig(updates.getConfig());
        }
        
        AgentTeam saved = teamRepository.save(existing);
        
        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT,
            Map.of("component", "AgentTeamService", "teamId", id),
            "TEAM_UPDATED",
            Map.of("name", saved.getName()),
            null,
            null
        );
        
        return saved;
    }

    @Transactional
    public void deleteById(String id) {
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("AgentTeam", id);
        }
        
        teamRepository.deleteById(id);
        
        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT,
            Map.of("component", "AgentTeamService", "teamId", id),
            "TEAM_DELETED",
            Map.of(),
            null,
            null
        );
    }
}
