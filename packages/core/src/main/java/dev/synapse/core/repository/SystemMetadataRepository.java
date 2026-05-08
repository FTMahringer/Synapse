package dev.synapse.core.repository;

import dev.synapse.core.domain.SystemMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemMetadataRepository extends JpaRepository<SystemMetadata, Boolean> {
}
