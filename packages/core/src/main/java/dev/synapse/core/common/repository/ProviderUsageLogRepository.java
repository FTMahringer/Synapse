package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.ProviderUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProviderUsageLogRepository extends JpaRepository<ProviderUsageLog, UUID> {

    List<ProviderUsageLog> findByProviderIdOrderByCreatedAtDesc(UUID providerId);

    List<ProviderUsageLog> findByProviderIdAndCreatedAtAfterOrderByCreatedAtDesc(
        UUID providerId, 
        LocalDateTime after
    );

    @Query("SELECT AVG(p.latencyMs) FROM ProviderUsageLog p WHERE p.providerId = :providerId AND p.success = true")
    Double averageLatencyForProvider(@Param("providerId") UUID providerId);

    @Query("SELECT SUM(p.totalTokens) FROM ProviderUsageLog p WHERE p.providerId = :providerId")
    Long totalTokensForProvider(@Param("providerId") UUID providerId);

    @Query("SELECT COUNT(p) FROM ProviderUsageLog p WHERE p.providerId = :providerId AND p.success = false")
    Long failureCountForProvider(@Param("providerId") UUID providerId);
}
