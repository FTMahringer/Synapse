package dev.synapse.agents;

import dev.synapse.core.common.domain.AgentMemoryEntry.MemoryTier;
import dev.synapse.core.common.domain.AgentMemoryEntry.PromotionReason;
import dev.synapse.core.dto.AgentMemoryEntryDTO;
import dev.synapse.core.dto.DtoMapper;
import dev.synapse.core.dto.PromoteMemoryRequest;
import dev.synapse.core.dto.WriteMemoryRequest;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.exception.ValidationException;
import dev.synapse.agents.service.AgentMemoryService;
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
        @RequestParam(required = false) String namespace,
        @RequestParam(required = false) String tier
    ) {
        var entries = tier != null
            ? memoryService.readByTier(agentId, namespace, parseTier(tier))
            : (namespace != null
                ? memoryService.readByNamespace(agentId, namespace)
                : memoryService.readAll(agentId));
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

    @PostMapping("/{key}/promote")
    public AgentMemoryEntryDTO promoteMemory(
        @PathVariable String agentId,
        @PathVariable String key,
        @Valid @RequestBody PromoteMemoryRequest request
    ) {
        var entry = memoryService.promote(
            agentId,
            key,
            parseTier(request.targetTier()),
            parseReason(request.reason())
        );
        return DtoMapper.toDTO(entry);
    }

    private MemoryTier parseTier(String tier) {
        try {
            return MemoryTier.valueOf(tier.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown memory tier: " + tier);
        }
    }

    private PromotionReason parseReason(String reason) {
        try {
            return PromotionReason.valueOf(reason.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown promotion reason: " + reason);
        }
    }
}
