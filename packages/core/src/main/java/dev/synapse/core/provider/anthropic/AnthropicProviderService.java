package dev.synapse.core.provider.anthropic;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.synapse.core.common.domain.ModelProvider;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.provider.ModelProviderService;
import dev.synapse.core.provider.ProviderUsageLogService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class AnthropicProviderService {

    private final ModelProviderService providerService;
    private final SystemLogService logService;
    private final ProviderUsageLogService usageLogService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public AnthropicProviderService(
        ModelProviderService providerService,
        SystemLogService logService,
        ProviderUsageLogService usageLogService,
        ObjectMapper objectMapper
    ) {
        this.providerService = providerService;
        this.logService = logService;
        this.usageLogService = usageLogService;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public boolean checkHealth(ModelProvider provider) {
        String apiKey = getApiKey(provider);
        
        try {
            AnthropicModels.ChatRequest testRequest = new AnthropicModels.ChatRequest(
                "claude-3-5-haiku-20241022",
                10,
                java.util.List.of(new AnthropicModels.Message("user", "test")),
                null,
                null,
                null,
                false
            );
            
            restClient.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("Content-Type", "application/json")
                .body(testRequest)
                .retrieve()
                .body(AnthropicModels.ChatResponse.class);
            
            logService.log(
                LogLevel.INFO,
                LogCategory.MODEL,
                Map.of("component", "AnthropicProviderService", "providerId", provider.getId().toString()),
                "PROVIDER_HEALTH_CHECK",
                Map.of("provider", provider.getName(), "status", "healthy"),
                null,
                null
            );
            
            return true;
        } catch (Exception e) {
            logService.log(
                LogLevel.WARN,
                LogCategory.MODEL,
                Map.of("component", "AnthropicProviderService", "providerId", provider.getId().toString()),
                "PROVIDER_HEALTH_CHECK_FAILED",
                Map.of("provider", provider.getName(), "error", e.getMessage()),
                null,
                null
            );
            
            return false;
        }
    }

    public AnthropicModels.ChatResponse chatCompletion(ModelProvider provider, AnthropicModels.ChatRequest request) {
        String apiKey = getApiKey(provider);
        long startTime = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        AnthropicModels.ChatResponse response = null;
        
        try {
            response = restClient.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("Content-Type", "application/json")
                .body(request)
                .retrieve()
                .body(AnthropicModels.ChatResponse.class);
            
            success = true;
            long duration = System.currentTimeMillis() - startTime;
            
            logService.log(
                LogLevel.INFO,
                LogCategory.MODEL,
                Map.of(
                    "component", "AnthropicProviderService",
                    "providerId", provider.getId().toString(),
                    "model", request.model()
                ),
                "CHAT_COMPLETION",
                Map.of(
                    "provider", provider.getName(),
                    "duration_ms", duration,
                    "input_tokens", response.usage() != null ? response.usage().inputTokens() : 0,
                    "output_tokens", response.usage() != null ? response.usage().outputTokens() : 0
                ),
                null,
                null
            );
            
            return response;
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            long duration = System.currentTimeMillis() - startTime;
            
            logService.log(
                LogLevel.ERROR,
                LogCategory.MODEL,
                Map.of(
                    "component", "AnthropicProviderService",
                    "providerId", provider.getId().toString(),
                    "model", request.model()
                ),
                "CHAT_COMPLETION_FAILED",
                Map.of(
                    "provider", provider.getName(),
                    "error", e.getMessage(),
                    "duration_ms", duration
                ),
                null,
                null
            );
            
            throw new RuntimeException("Failed to complete Anthropic chat", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            usageLogService.logUsage(
                provider,
                request.model(),
                response != null && response.usage() != null ? response.usage().inputTokens() : null,
                response != null && response.usage() != null ? response.usage().outputTokens() : null,
                duration,
                success,
                errorMessage
            );
        }
    }

    private String getApiKey(ModelProvider provider) {
        Map<String, String> secrets = providerService.decryptSecrets(provider);
        String apiKey = secrets.get("apiKey");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("API key not configured for provider: " + provider.getName());
        }
        return apiKey;
    }
}
