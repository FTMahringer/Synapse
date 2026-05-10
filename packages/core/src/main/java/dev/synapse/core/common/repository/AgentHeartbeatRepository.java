package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.AgentHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentHeartbeatRepository extends JpaRepository<AgentHeartbeat, UUID> {
    List<AgentHeartbeat> findByAgentIdOrderByRecordedAtDesc(String agentId);

    Optional<AgentHeartbeat> findTopByAgentIdOrderByRecordedAtDesc(String agentId);

    @Query("SELECT DISTINCT h.agentId FROM AgentHeartbeat h WHERE h.recordedAt >= :since")
    List<String> findActiveAgentIdsSince(@Param("since") Instant since);

    List<AgentHeartbeat> findByAgentIdAndRecordedAtAfterOrderByRecordedAtDesc(String agentId, Instant since);
}
