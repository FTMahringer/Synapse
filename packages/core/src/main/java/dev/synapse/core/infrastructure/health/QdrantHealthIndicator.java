package dev.synapse.core.infrastructure.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component("qdrant")
public class QdrantHealthIndicator implements HealthIndicator {

    private final RestClient restClient;
    private final String qdrantUrl;
    private final boolean enabled;

    public QdrantHealthIndicator(
        @Value("${QDRANT_URL:http://localhost:6333}") String qdrantUrl,
        @Value("${QDRANT_HEALTH_ENABLED:true}") boolean enabled
    ) {
        this.restClient = RestClient.builder().build();
        this.qdrantUrl = qdrantUrl;
        this.enabled = enabled;
    }

    @Override
    public Health health() {
        if (!enabled) {
            return Health.unknown()
                .withDetail("enabled", false)
                .withDetail("reason", "Qdrant health check disabled")
                .build();
        }

        try {
            restClient.get()
                .uri(qdrantUrl + "/healthz")
                .retrieve()
                .toBodilessEntity();

            return Health.up()
                .withDetail("url", qdrantUrl)
                .build();
        } catch (Exception ex) {
            return Health.down(ex)
                .withDetail("url", qdrantUrl)
                .build();
        }
    }
}

