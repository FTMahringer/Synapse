package dev.synapse.tools;

import dev.synapse.core.dto.DtoMapper;
import dev.synapse.core.dto.ExecuteToolRequest;
import dev.synapse.core.dto.ToolDefinitionDTO;
import dev.synapse.core.dto.ToolExecutionResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
public class NativeToolController {

    private final ToolRegistryService toolRegistryService;
    private final ToolExecutionService toolExecutionService;

    public NativeToolController(
        ToolRegistryService toolRegistryService,
        ToolExecutionService toolExecutionService
    ) {
        this.toolRegistryService = toolRegistryService;
        this.toolExecutionService = toolExecutionService;
    }

    @GetMapping
    public List<ToolDefinitionDTO> listTools() {
        return toolRegistryService.listToolDefinitions().stream().map(DtoMapper::toDTO).toList();
    }

    @GetMapping("/{toolId}")
    public ToolDefinitionDTO getTool(@PathVariable String toolId) {
        return DtoMapper.toDTO(toolRegistryService.getDefinition(toolId));
    }

    @PostMapping("/{toolId}/execute")
    public ToolExecutionResponseDTO execute(
        @PathVariable String toolId,
        @RequestBody(required = false) ExecuteToolRequest request
    ) {
        ExecuteToolRequest effectiveRequest = request != null
            ? request
            : new ExecuteToolRequest(null, null, null, null, null);
        var context = new ToolExecutionContext(
            effectiveRequest.agentId(),
            effectiveRequest.teamId(),
            effectiveRequest.collaborationSessionId(),
            effectiveRequest.goalId()
        );
        var response = toolExecutionService.execute(toolId, context, effectiveRequest.input());
        return DtoMapper.toDTO(response);
    }
}
