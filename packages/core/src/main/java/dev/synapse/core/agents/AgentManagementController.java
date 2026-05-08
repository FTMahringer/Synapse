package dev.synapse.core.agents;

import dev.synapse.core.dto.AgentDTO;
import dev.synapse.core.dto.CreateAgentRequest;
import dev.synapse.core.dto.DtoMapper;
import dev.synapse.core.service.AgentManagementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agents")
public class AgentManagementController {

    private final AgentManagementService agentManagementService;

    public AgentManagementController(
        AgentManagementService agentManagementService
    ) {
        this.agentManagementService = agentManagementService;
    }

    @GetMapping
    public List<AgentDTO> listAgents() {
        return agentManagementService
            .listAllAgents()
            .stream()
            .map(DtoMapper::toDTO)
            .toList();
    }

    @GetMapping("/{id}")
    public AgentDTO getAgent(@PathVariable String id) {
        return DtoMapper.toDTO(agentManagementService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentDTO createAgent(
        @Valid @RequestBody CreateAgentRequest request
    ) {
        var agent = DtoMapper.fromCreateRequest(request);
        var created = agentManagementService.createAgent(agent);
        return DtoMapper.toDTO(created);
    }

    @PutMapping("/{id}")
    public AgentDTO updateAgent(
        @PathVariable String id,
        @Valid @RequestBody CreateAgentRequest request
    ) {
        var updates = DtoMapper.fromCreateRequest(request);
        var updated = agentManagementService.updateAgent(id, updates);
        return DtoMapper.toDTO(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAgent(@PathVariable String id) {
        agentManagementService.deleteAgent(id);
    }

    @PostMapping("/_sync")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void syncFileAgents() {
        agentManagementService.syncFileAgentsToDatabase();
    }
}
