package dev.synapse.tasks;

import dev.synapse.core.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskDTO> listTasks(
        @RequestParam(required = false) UUID projectId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        if (projectId != null) {
            return taskService.findByProjectId(projectId, page, size).stream()
                .map(DtoMapper::toDTO)
                .toList();
        }
        return taskService.findAll(page, size).stream()
            .map(DtoMapper::toDTO)
            .toList();
    }

    @GetMapping("/{id}")
    public TaskDTO getTask(@PathVariable UUID id) {
        return DtoMapper.toDTO(taskService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO createTask(@Valid @RequestBody CreateTaskRequest request) {
        var task = DtoMapper.fromCreateRequest(request);
        var created = taskService.save(task);
        return DtoMapper.toDTO(created);
    }

    @PatchMapping("/{id}")
    public TaskDTO updateTask(@PathVariable UUID id, @Valid @RequestBody UpdateTaskRequest request) {
        var existing = taskService.findById(id);
        
        if (request.title() != null) {
            existing.setTitle(request.title());
        }
        if (request.status() != null) {
            existing.setStatus(dev.synapse.core.common.domain.Task.TaskStatus.valueOf(request.status()));
        }
        if (request.assignedAgentId() != null) {
            existing.setAssignedAgentId(request.assignedAgentId());
        }
        if (request.size() != null) {
            existing.setSize(dev.synapse.core.common.domain.Task.TaskSize.valueOf(request.size()));
        }
        
        var updated = taskService.update(id, existing);
        return DtoMapper.toDTO(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable UUID id) {
        taskService.deleteById(id);
    }

    @GetMapping("/{id}/logs")
    public List<TaskLogDTO> getTaskLogs(@PathVariable UUID id) {
        return taskService.getTaskLogs(id).stream()
            .map(log -> new TaskLogDTO(
                log.getId(),
                log.getTaskId(),
                log.getEvent(),
                log.getPayload(),
                log.getCreatedAt()
            ))
            .toList();
    }
}
