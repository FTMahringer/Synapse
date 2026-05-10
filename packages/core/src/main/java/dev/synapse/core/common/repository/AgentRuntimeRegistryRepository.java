package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.AgentRuntimeRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentRuntimeRegistryRepository extends JpaRepository<AgentRuntimeRegistry, UUID> {
    
    Optional<AgentRuntimeRegistry> findByAgentId(String agentId);
}
