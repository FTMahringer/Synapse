package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.CollaborationDelegation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CollaborationDelegationRepository extends JpaRepository<CollaborationDelegation, UUID> {
    List<CollaborationDelegation> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
}
