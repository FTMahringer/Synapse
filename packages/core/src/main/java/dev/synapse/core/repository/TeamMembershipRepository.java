package dev.synapse.core.repository;

import dev.synapse.core.domain.TeamMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamMembershipRepository extends JpaRepository<TeamMembership, UUID> {
    
    List<TeamMembership> findByTeamId(String teamId);
    
    List<TeamMembership> findByAgentId(String agentId);
}
