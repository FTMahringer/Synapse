package dev.synapse.core.repository;

import dev.synapse.core.domain.Agent;
import dev.synapse.core.domain.Agent.AgentType;
import dev.synapse.core.domain.Agent.AgentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, String> {
    List<Agent> findByType(AgentType type);
    List<Agent> findByStatus(AgentStatus status);
    List<Agent> findByTypeAndStatus(AgentType type, AgentStatus status);
}
