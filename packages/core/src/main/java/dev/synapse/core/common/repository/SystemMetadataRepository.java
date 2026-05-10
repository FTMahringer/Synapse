package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.SystemMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemMetadataRepository extends JpaRepository<SystemMetadata, Boolean> {
}
