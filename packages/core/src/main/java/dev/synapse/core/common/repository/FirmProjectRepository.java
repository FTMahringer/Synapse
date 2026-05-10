package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.FirmProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FirmProjectRepository extends JpaRepository<FirmProject, UUID> {
    List<FirmProject> findByFirmIdOrderByCreatedAtDesc(String firmId);
    List<FirmProject> findByStatusOrderByCreatedAtDesc(FirmProject.FirmProjectStatus status);
}
