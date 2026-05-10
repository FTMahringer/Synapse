package dev.synapse.core.infrastructure.health;

import dev.synapse.core.common.domain.ModelProvider;
import dev.synapse.providers.ModelProviderService;
import dev.synapse.providers.anthropic.AnthropicProviderService;
import dev.synapse.providers.ollama.OllamaProviderService;
import dev.synapse.providers.openai.OpenAIProviderService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("providers")
public class ProviderHealthIndicator implements HealthIndicator {

    private final ModelProviderService modelProviderService;
    private final OpenAIProviderService openAIProviderService;
    private final AnthropicProviderService anthropicProviderService;
    private final OllamaProviderService ollamaProviderService;

    public ProviderHealthIndicator(
        ModelProviderService modelProviderService,
        OpenAIProviderService openAIProviderService,
        AnthropicProviderService anthropicProviderService,
        OllamaProviderService ollamaProviderService
    ) {
        this.modelProviderService = modelProviderService;
        this.openAIProviderService = openAIProviderService;
        this.anthropicProviderService = anthropicProviderService;
        this.ollamaProviderService = ollamaProviderService;
    }

    @Override
    public Health health() {
        List<ModelProvider> providers = modelProviderService.findEnabled();
        if (providers.isEmpty()) {
            return Health.up()
                .withDetail("enabledProviders", 0)
                .withDetail("status", "no providers enabled")
                .build();
        }

        Map<String, Object> details = new LinkedHashMap<>();
        int healthyCount = 0;

        for (ModelProvider provider : providers) {
            boolean healthy = checkProvider(provider);
            details.put(provider.getName(), Map.of(
                "type", provider.getType().name(),
                "healthy", healthy
            ));
            if (healthy) {
                healthyCount++;
            }
        }

        details.put("healthyProviders", healthyCount);
        details.put("enabledProviders", providers.size());

        if (healthyCount == providers.size()) {
            return Health.up().withDetails(details).build();
        }
        return Health.down().withDetails(details).build();
    }

    private boolean checkProvider(ModelProvider provider) {
        return switch (provider.getType()) {
            case OPENAI, OPENAI_COMPATIBLE -> openAIProviderService.checkHealth(provider);
            case ANTHROPIC -> anthropicProviderService.checkHealth(provider);
            case OLLAMA -> ollamaProviderService.checkHealth(provider);
        };
    }
}

