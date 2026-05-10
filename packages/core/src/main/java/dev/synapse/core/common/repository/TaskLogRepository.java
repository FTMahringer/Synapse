package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLog, UUID> {
    List<TaskLog> findByTaskIdOrderByCreatedAtDesc(UUID taskId);
}
