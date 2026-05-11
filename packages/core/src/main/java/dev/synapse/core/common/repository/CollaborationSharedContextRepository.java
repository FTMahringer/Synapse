package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.CollaborationSharedContextEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollaborationSharedContextRepository extends JpaRepository<CollaborationSharedContextEntry, UUID> {
    List<CollaborationSharedContextEntry> findBySessionIdOrderByUpdatedAtDesc(UUID sessionId);
    Optional<CollaborationSharedContextEntry> findBySessionIdAndContextKey(UUID sessionId, String contextKey);
}
