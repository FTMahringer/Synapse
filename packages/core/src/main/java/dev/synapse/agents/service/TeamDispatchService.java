package dev.synapse.agents.service;

import dev.synapse.core.common.domain.TeamMembership;
import dev.synapse.core.common.repository.TeamMembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Team dispatch service for routing to team leaders and members.
 * Manages team membership and dispatch logic.
 */
@Service
public class TeamDispatchService {

    private final TeamMembershipRepository teamMembershipRepository;
    private final AgentRuntimeService agentRuntimeService;

    public TeamDispatchService(
        TeamMembershipRepository teamMembershipRepository,
        AgentRuntimeService agentRuntimeService
    ) {
        this.teamMembershipRepository = teamMembershipRepository;
        this.agentRuntimeService = agentRuntimeService;
    }

    /**
     * Dispatch a request to a team leader.
     * Checks if leader is active before dispatching.
     * 
     * @param teamId Team ID
     * @param conversationId Conversation ID
     * @param messageContent Message content
     * @return Dispatch result with leader agent ID
     */
    @Transactional(readOnly = true)
    public DispatchResult dispatchToLeader(String teamId, UUID conversationId, String messageContent) {
        List<TeamMembership> memberships = teamMembershipRepository.findByTeamId(teamId);
        
        TeamMembership leader = memberships.stream()
            .filter(m -> m.getRole() == TeamMembership.TeamRole.LEADER)
            .findFirst()
            .orElse(null);
        
        if (leader == null) {
            return new DispatchResult(false, null, "No team leader found for team: " + teamId);
        }
        
        if (!agentRuntimeService.isActive(leader.getAgentId())) {
            return new DispatchResult(false, leader.getAgentId(), "Team leader is not active: " + leader.getAgentId());
        }
        
        return new DispatchResult(true, leader.getAgentId(), "Dispatched to team leader");
    }

    /**
     * Dispatch a request to a team member (not the leader).
     * Selects first active member.
     * 
     * @param teamId Team ID
     * @param conversationId Conversation ID
     * @param messageContent Message content
     * @return Dispatch result with member agent ID
     */
    @Transactional(readOnly = true)
    public DispatchResult dispatchToMember(String teamId, UUID conversationId, String messageContent) {
        List<TeamMembership> memberships = teamMembershipRepository.findByTeamId(teamId);
        
        TeamMembership member = memberships.stream()
            .filter(m -> m.getRole() == TeamMembership.TeamRole.MEMBER)
            .filter(m -> agentRuntimeService.isActive(m.getAgentId()))
            .findFirst()
            .orElse(null);
        
        if (member == null) {
            return new DispatchResult(false, null, "No active team members found for team: " + teamId);
        }
        
        return new DispatchResult(true, member.getAgentId(), "Dispatched to team member");
    }

    /**
     * Get all members of a team.
     */
    public List<TeamMembership> getTeamMembers(String teamId) {
        return teamMembershipRepository.findByTeamId(teamId);
    }

    /**
     * Get all teams an agent belongs to.
     */
    public List<TeamMembership> getAgentTeams(String agentId) {
        return teamMembershipRepository.findByAgentId(agentId);
    }

    /**
     * Result of a team dispatch operation.
     */
    public record DispatchResult(
        boolean success,
        String targetAgentId,
        String message
    ) {}
}
