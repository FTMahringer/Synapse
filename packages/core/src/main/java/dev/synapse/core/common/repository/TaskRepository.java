package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.Task;
import dev.synapse.core.common.domain.Task.TaskStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByProjectId(UUID projectId);
    List<Task> findByProjectId(UUID projectId, Pageable pageable);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByAssignedAgentId(String assignedAgentId);
    List<Task> findByProjectIdAndStatus(UUID projectId, TaskStatus status);
}
