package dev.synapse.core.bootstrap;

import dev.synapse.core.common.domain.ModelProvider;
import dev.synapse.core.infrastructure.config.SynapseProperties;
import dev.synapse.providers.ModelProviderService;
import dev.synapse.providers.anthropic.AnthropicProviderService;
import dev.synapse.providers.ollama.OllamaProviderService;
import dev.synapse.providers.openai.OpenAIProviderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final SynapseProperties properties;
    private final JdbcClient jdbcClient;
    private final StringRedisTemplate redisTemplate;
    private final ModelProviderService modelProviderService;
    private final OpenAIProviderService openAIProviderService;
    private final AnthropicProviderService anthropicProviderService;
    private final OllamaProviderService ollamaProviderService;
    private final RestClient restClient;
    private final String qdrantUrl;
    private final boolean qdrantHealthEnabled;

    public HealthController(
        SynapseProperties properties,
        JdbcClient jdbcClient,
        StringRedisTemplate redisTemplate,
        ModelProviderService modelProviderService,
        OpenAIProviderService openAIProviderService,
        AnthropicProviderService anthropicProviderService,
        OllamaProviderService ollamaProviderService,
        @Value("${QDRANT_URL:http://localhost:6333}") String qdrantUrl,
        @Value("${QDRANT_HEALTH_ENABLED:true}") boolean qdrantHealthEnabled
    ) {
        this.properties = properties;
        this.jdbcClient = jdbcClient;
        this.redisTemplate = redisTemplate;
        this.modelProviderService = modelProviderService;
        this.openAIProviderService = openAIProviderService;
        this.anthropicProviderService = anthropicProviderService;
        this.ollamaProviderService = ollamaProviderService;
        this.restClient = RestClient.builder().build();
        this.qdrantUrl = qdrantUrl;
        this.qdrantHealthEnabled = qdrantHealthEnabled;
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

    @GetMapping("/liveness")
    public ProbeResponse liveness() {
        return new ProbeResponse("UP", Map.of("application", "UP"), Instant.now());
    }

    @GetMapping("/readiness")
    public ProbeResponse readiness() {
        Map<String, String> checks = new LinkedHashMap<>();
        checks.put("database", checkDatabase() ? "UP" : "DOWN");
        checks.put("redis", checkRedis() ? "UP" : "DOWN");
        checks.put("qdrant", checkQdrant() ? "UP" : "DOWN");
        checks.put("providers", checkProviders() ? "UP" : "DOWN");

        boolean up = checks.values().stream().allMatch("UP"::equals);
        return new ProbeResponse(up ? "UP" : "DOWN", checks, Instant.now());
    }

    private boolean checkDatabase() {
        try {
            Integer result = jdbcClient.sql("SELECT 1").query(Integer.class).single();
            return result != null && result == 1;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            String pong = redisTemplate.execute(connection -> connection.ping());
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean checkQdrant() {
        if (!qdrantHealthEnabled) {
            return true;
        }
        try {
            restClient.get()
                .uri(qdrantUrl + "/healthz")
                .retrieve()
                .toBodilessEntity();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean checkProviders() {
        try {
            List<ModelProvider> providers = modelProviderService.findEnabled();
            if (providers.isEmpty()) {
                return true;
            }

            for (ModelProvider provider : providers) {
                boolean healthy = switch (provider.getType()) {
                    case OPENAI, OPENAI_COMPATIBLE -> openAIProviderService.checkHealth(provider);
                    case ANTHROPIC -> anthropicProviderService.checkHealth(provider);
                    case OLLAMA -> ollamaProviderService.checkHealth(provider);
                };
                if (!healthy) {
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
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

    public record ProbeResponse(
        String status,
        Map<String, String> checks,
        Instant timestamp
    ) {}
}
