package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.AgentMemoryEntry;
import dev.synapse.core.common.domain.AgentMemoryEntry.MemoryTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentMemoryRepository extends JpaRepository<AgentMemoryEntry, UUID> {
    List<AgentMemoryEntry> findByAgentIdOrderByKeyAsc(String agentId);
    List<AgentMemoryEntry> findByAgentIdAndNamespaceOrderByKeyAsc(String agentId, String namespace);
    List<AgentMemoryEntry> findByAgentIdAndTierOrderByKeyAsc(String agentId, MemoryTier tier);
    List<AgentMemoryEntry> findByAgentIdAndNamespaceAndTierOrderByKeyAsc(String agentId, String namespace, MemoryTier tier);
    List<AgentMemoryEntry> findByTierAndUpdatedAtBefore(MemoryTier tier, Instant updatedBefore);
    Optional<AgentMemoryEntry> findByAgentIdAndKey(String agentId, String key);
    void deleteByAgentIdAndKey(String agentId, String key);
}
