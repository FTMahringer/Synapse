package dev.synapse.core.repository;

import dev.synapse.core.domain.Project;
import dev.synapse.core.domain.Project.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByStatus(ProjectStatus status);
    List<Project> findByAiFirmId(String aiFirmId);
}
