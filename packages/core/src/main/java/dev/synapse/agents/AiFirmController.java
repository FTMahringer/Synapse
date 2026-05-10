package dev.synapse.agents;

import dev.synapse.core.dto.DispatchProjectRequest;
import dev.synapse.core.dto.DtoMapper;
import dev.synapse.core.dto.FirmProjectDTO;
import dev.synapse.agents.service.AiFirmDispatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/firm")
public class AiFirmController {

    private final AiFirmDispatchService dispatchService;

    public AiFirmController(AiFirmDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @GetMapping("/projects")
    public List<FirmProjectDTO> listProjects() {
        return dispatchService.findAll().stream().map(DtoMapper::toDTO).toList();
    }

    @GetMapping("/projects/{id}")
    public FirmProjectDTO getProject(@PathVariable UUID id) {
        return DtoMapper.toDTO(dispatchService.findById(id));
    }

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public FirmProjectDTO dispatchProject(@Valid @RequestBody DispatchProjectRequest request) {
        var project = dispatchService.dispatchProject(
            request.title(),
            request.description(),
            request.dispatchedByAgentId(),
            request.conversationId(),
            request.taskId()
        );
        return DtoMapper.toDTO(project);
    }

    @PostMapping("/projects/{id}/assign")
    public FirmProjectDTO assignToTeam(
        @PathVariable UUID id,
        @RequestParam String teamId
    ) {
        return DtoMapper.toDTO(dispatchService.assignToTeam(id, teamId));
    }

    @PostMapping("/projects/{id}/complete")
    public FirmProjectDTO completeProject(@PathVariable UUID id) {
        return DtoMapper.toDTO(dispatchService.completeProject(id));
    }
}
