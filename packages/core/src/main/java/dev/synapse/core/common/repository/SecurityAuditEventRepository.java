package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.SecurityAuditEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityAuditEventRepository extends JpaRepository<SecurityAuditEvent, UUID> {

    List<SecurityAuditEvent> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    List<SecurityAuditEvent> findByEventTypeOrderByTimestampDesc(String eventType, Pageable pageable);

    List<SecurityAuditEvent> findByTimestampBetween(Instant from, Instant to, Pageable pageable);
}
