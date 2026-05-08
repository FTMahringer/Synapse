package dev.synapse.core.agents;

import dev.synapse.core.dto.AgentTeamDTO;
import dev.synapse.core.dto.CreateAgentTeamRequest;
import dev.synapse.core.dto.DtoMapper;
import dev.synapse.core.service.AgentTeamService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
public class AgentTeamController {

    private final AgentTeamService teamService;

    public AgentTeamController(AgentTeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public List<AgentTeamDTO> listTeams() {
        return teamService.findAll().stream().map(DtoMapper::toDTO).toList();
    }

    @GetMapping("/{id}")
    public AgentTeamDTO getTeam(@PathVariable String id) {
        return DtoMapper.toDTO(teamService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentTeamDTO createTeam(
        @Valid @RequestBody CreateAgentTeamRequest request
    ) {
        var team = DtoMapper.fromCreateRequest(request);
        var created = teamService.save(team);
        return DtoMapper.toDTO(created);
    }

    @PutMapping("/{id}")
    public AgentTeamDTO updateTeam(
        @PathVariable String id,
        @Valid @RequestBody CreateAgentTeamRequest request
    ) {
        var updates = DtoMapper.fromCreateRequest(request);
        var updated = teamService.update(id, updates);
        return DtoMapper.toDTO(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeam(@PathVariable String id) {
        teamService.deleteById(id);
    }
}
