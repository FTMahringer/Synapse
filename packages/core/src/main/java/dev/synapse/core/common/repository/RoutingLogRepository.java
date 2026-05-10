package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.RoutingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoutingLogRepository extends JpaRepository<RoutingLog, UUID> {
    
    List<RoutingLog> findByConversationIdOrderByCreatedAtDesc(UUID conversationId);
}
