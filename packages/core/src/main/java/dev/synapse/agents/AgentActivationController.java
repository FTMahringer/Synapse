package dev.synapse.agents;

import dev.synapse.core.common.domain.AgentHeartbeat;
import dev.synapse.core.common.domain.AgentRuntimeRegistry;
import dev.synapse.core.common.domain.RoutingLog;
import dev.synapse.agents.service.AgentHeartbeatService;
import dev.synapse.agents.service.AgentRuntimeService;
import dev.synapse.agents.service.MainAgentRouterService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Agent activation management and routing inspection endpoints.
 * Used by the dashboard to manage agent states and inspect routing decisions.
 */
@RestController
@RequestMapping("/api/agents")
public class AgentActivationController {

    private final AgentRuntimeService runtimeService;
    private final AgentHeartbeatService heartbeatService;
    private final MainAgentRouterService routerService;

    public AgentActivationController(
        AgentRuntimeService runtimeService,
        AgentHeartbeatService heartbeatService,
        MainAgentRouterService routerService
    ) {
        this.runtimeService = runtimeService;
        this.heartbeatService = heartbeatService;
        this.routerService = routerService;
    }

    @GetMapping("/runtime")
    public List<AgentRuntimeRegistry> listRuntimeStates() {
        return runtimeService.findAll();
    }

    @GetMapping("/{agentId}/runtime")
    public AgentRuntimeRegistry getRuntimeState(@PathVariable String agentId) {
        return runtimeService.getOrCreateRuntime(agentId);
    }

    @PostMapping("/{agentId}/activate")
    public AgentRuntimeRegistry activate(@PathVariable String agentId) {
        return runtimeService.activate(agentId);
    }

    @PostMapping("/{agentId}/pause")
    public AgentRuntimeRegistry pause(@PathVariable String agentId) {
        return runtimeService.pause(agentId);
    }

    @PostMapping("/{agentId}/disable")
    public AgentRuntimeRegistry disable(@PathVariable String agentId) {
        return runtimeService.disable(agentId);
    }

    @PostMapping("/{agentId}/heartbeat")
    @ResponseStatus(HttpStatus.CREATED)
    public AgentHeartbeat pulse(
        @PathVariable String agentId,
        @RequestParam(required = false) String note
    ) {
        return heartbeatService.pulse(agentId, note != null ? note : "manual");
    }

    @GetMapping("/{agentId}/heartbeat")
    public Map<String, Object> getHeartbeatStatus(@PathVariable String agentId) {
        Optional<AgentHeartbeat> latest = heartbeatService.getLatest(agentId);
        boolean alive = heartbeatService.isAlive(agentId);
        return Map.of(
            "agentId", agentId,
            "alive", alive,
            "lastHeartbeat", latest.map(h -> h.getRecordedAt().toString()).orElse("never")
        );
    }

    @GetMapping("/alive")
    public List<String> getAliveAgents() {
        return heartbeatService.getAliveAgentIds();
    }

    @GetMapping("/routing")
    public List<RoutingLog> getAllRoutingLogs() {
        return routerService.getAllRoutingLogs();
    }

    @GetMapping("/routing/{conversationId}")
    public List<RoutingLog> getRoutingLogsForConversation(@PathVariable UUID conversationId) {
        return routerService.getRoutingHistory(conversationId);
    }
}
