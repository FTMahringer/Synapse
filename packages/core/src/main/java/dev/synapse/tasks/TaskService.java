package dev.synapse.tasks;

import dev.synapse.core.common.domain.Task;
import dev.synapse.core.common.domain.TaskLog;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.common.repository.TaskLogRepository;
import dev.synapse.core.common.repository.TaskRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;
    private final SystemLogService logService;

    public TaskService(
        TaskRepository taskRepository,
        TaskLogRepository taskLogRepository,
        SystemLogService logService
    ) {
        this.taskRepository = taskRepository;
        this.taskLogRepository = taskLogRepository;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Task> findAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return taskRepository.findAll(pageRequest).getContent();
    }

    @Transactional(readOnly = true)
    public Task findById(UUID id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task", id.toString()));
    }

    @Transactional(readOnly = true)
    public List<Task> findByProjectId(UUID projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<Task> findByProjectId(UUID projectId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return taskRepository.findByProjectId(projectId, pageRequest);
    }

    @Transactional
    public Task save(Task task) {
        Task saved = taskRepository.save(task);
        
        logService.log(
            LogLevel.INFO,
            LogCategory.TASK,
            Map.of("component", "TaskService", "taskId", saved.getId().toString()),
            "TASK_CREATED",
            Map.of("title", task.getTitle(), "projectId", task.getProjectId().toString()),
            null,
            null
        );
        
        addTaskLog(saved.getId(), "TASK_CREATED", Map.of("title", task.getTitle()));
        
        return saved;
    }

    @Transactional
    public Task update(UUID id, Task updates) {
        Task existing = findById(id);
        
        boolean changed = false;
        Map<String, Object> changes = new java.util.HashMap<>();
        
        if (updates.getTitle() != null && !updates.getTitle().equals(existing.getTitle())) {
            changes.put("title", Map.of("from", existing.getTitle(), "to", updates.getTitle()));
            existing.setTitle(updates.getTitle());
            changed = true;
        }
        
        if (updates.getStatus() != null && updates.getStatus() != existing.getStatus()) {
            changes.put("status", Map.of("from", existing.getStatus().name(), "to", updates.getStatus().name()));
            existing.setStatus(updates.getStatus());
            changed = true;
        }
        
        if (updates.getAssignedAgentId() != null && !updates.getAssignedAgentId().equals(existing.getAssignedAgentId())) {
            changes.put("assignedAgentId", Map.of("from", existing.getAssignedAgentId(), "to", updates.getAssignedAgentId()));
            existing.setAssignedAgentId(updates.getAssignedAgentId());
            changed = true;
        }
        
        if (updates.getSize() != null && updates.getSize() != existing.getSize()) {
            changes.put("size", Map.of("from", existing.getSize(), "to", updates.getSize().name()));
            existing.setSize(updates.getSize());
            changed = true;
        }
        
        if (!changed) {
            return existing;
        }
        
        Task saved = taskRepository.save(existing);
        
        logService.log(
            LogLevel.INFO,
            LogCategory.TASK,
            Map.of("component", "TaskService", "taskId", id.toString()),
            "TASK_UPDATED",
            changes,
            null,
            null
        );
        
        addTaskLog(id, "TASK_UPDATED", changes);
        
        return saved;
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", id.toString());
        }
        
        taskRepository.deleteById(id);
        
        logService.log(
            LogLevel.INFO,
            LogCategory.TASK,
            Map.of("component", "TaskService", "taskId", id.toString()),
            "TASK_DELETED",
            Map.of(),
            null,
            null
        );
    }

    @Transactional(readOnly = true)
    public List<TaskLog> getTaskLogs(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task", taskId.toString());
        }
        return taskLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    @Transactional
    public TaskLog addTaskLog(UUID taskId, String event, Map<String, Object> payload) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task", taskId.toString());
        }
        
        TaskLog log = new TaskLog();
        log.setTaskId(taskId);
        log.setEvent(event);
        log.setPayload(payload != null ? payload : Map.of());
        
        return taskLogRepository.save(log);
    }
}
