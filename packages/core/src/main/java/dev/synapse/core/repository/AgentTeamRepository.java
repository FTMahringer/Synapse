package dev.synapse.core.repository;

import dev.synapse.core.domain.AgentTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentTeamRepository extends JpaRepository<AgentTeam, String> {
    List<AgentTeam> findByLeaderAgentId(String leaderAgentId);
}
