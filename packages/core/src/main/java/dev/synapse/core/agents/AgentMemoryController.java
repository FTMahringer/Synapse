package dev.synapse.core.agents;

import dev.synapse.core.dto.AgentMemoryEntryDTO;
import dev.synapse.core.dto.DtoMapper;
import dev.synapse.core.dto.WriteMemoryRequest;
import dev.synapse.core.exception.ResourceNotFoundException;
import dev.synapse.core.agents.service.AgentMemoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents/{agentId}/memory")
public class AgentMemoryController {

    private final AgentMemoryService memoryService;

    public AgentMemoryController(AgentMemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @GetMapping
    public List<AgentMemoryEntryDTO> listMemory(
        @PathVariable String agentId,
        @RequestParam(required = false) String namespace
    ) {
        var entries = namespace != null
            ? memoryService.readByNamespace(agentId, namespace)
            : memoryService.readAll(agentId);
        return entries.stream().map(DtoMapper::toDTO).toList();
    }

    @GetMapping("/{key}")
    public AgentMemoryEntryDTO getMemory(
        @PathVariable String agentId,
        @PathVariable String key
    ) {
        return memoryService.read(agentId, key)
            .map(DtoMapper::toDTO)
            .orElseThrow(() -> new ResourceNotFoundException("AgentMemoryEntry", agentId + "/" + key));
    }

    @PutMapping("/{key}")
    @ResponseStatus(HttpStatus.OK)
    public AgentMemoryEntryDTO writeMemory(
        @PathVariable String agentId,
        @PathVariable String key,
        @Valid @RequestBody WriteMemoryRequest request
    ) {
        var entry = memoryService.write(agentId, key, request.value(), request.namespace());
        return DtoMapper.toDTO(entry);
    }

    @DeleteMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMemory(
        @PathVariable String agentId,
        @PathVariable String key
    ) {
        memoryService.delete(agentId, key);
    }
}
