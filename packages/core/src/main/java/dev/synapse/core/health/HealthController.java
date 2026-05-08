package dev.synapse.core.health;

import dev.synapse.core.config.SynapseProperties;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final SynapseProperties properties;
    private final HealthEndpoint healthEndpoint;

    public HealthController(SynapseProperties properties, HealthEndpoint healthEndpoint) {
        this.properties = properties;
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping
    public HealthResponse health() {
        return new HealthResponse(
                properties.systemName(),
                properties.version(),
                healthEndpoint.health().getStatus().getCode(),
                properties.echo().enabled(),
                properties.echo().debugOnly(),
                properties.echo().activation(),
                Instant.now()
        );
    }

    public record HealthResponse(
            String systemName,
            String version,
            String status,
            boolean echoEnabled,
            boolean echoDebugOnly,
            String echoActivation,
            Instant timestamp
    ) {}
}
