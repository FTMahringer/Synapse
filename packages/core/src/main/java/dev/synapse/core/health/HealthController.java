package dev.synapse.core.health;

import dev.synapse.core.config.SynapseProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final SynapseProperties properties;

    public HealthController(SynapseProperties properties) {
        this.properties = properties;
    }

    @GetMapping
    public HealthResponse health() {
        return new HealthResponse(
                properties.systemName(),
                properties.version(),
                "UP",
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
