package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.AgentTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentTeamRepository extends JpaRepository<AgentTeam, String> {
    List<AgentTeam> findByLeaderAgentId(String leaderAgentId);
}
