package dev.synapse.core.agents;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentsController {

    private final AgentDefinitionLoader loader;

    public AgentsController(AgentDefinitionLoader loader) {
        this.loader = loader;
    }

    @GetMapping
    public List<AgentDefinition> listAgents() {
        return loader.listAgents();
    }
}
