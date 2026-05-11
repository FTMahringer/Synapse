package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.CollaborationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CollaborationMessageRepository extends JpaRepository<CollaborationMessage, UUID> {
    List<CollaborationMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
}
