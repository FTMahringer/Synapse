package dev.synapse.providers.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.synapse.core.common.domain.ModelProvider;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.providers.ModelProviderService;
import dev.synapse.providers.ProviderUsageLogService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIProviderService {

    private final ModelProviderService providerService;
    private final SystemLogService logService;
    private final ProviderUsageLogService usageLogService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAIProviderService(
        ModelProviderService providerService,
        SystemLogService logService,
        ProviderUsageLogService usageLogService,
        ObjectMapper objectMapper,
        RestClient restClient
    ) {
        this.providerService = providerService;
        this.logService = logService;
        this.usageLogService = usageLogService;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    public boolean checkHealth(ModelProvider provider) {
        String baseUrl = getBaseUrl(provider);
        String apiKey = getApiKey(provider);
        
        try {
            OpenAIModels.ModelsResponse response = restClient.get()
                .uri(baseUrl + "/v1/models")
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .body(OpenAIModels.ModelsResponse.class);
            
            logService.log(
                LogLevel.INFO,
                LogCategory.MODEL,
                Map.of("component", "OpenAIProviderService", "providerId", provider.getId().toString()),
                "PROVIDER_HEALTH_CHECK",
                Map.of("provider", provider.getName(), "status", "healthy"),
                null,
                null
            );
            
            return response != null && response.data() != null;
        } catch (Exception e) {
            logService.log(
                LogLevel.WARN,
                LogCategory.MODEL,
                Map.of("component", "OpenAIProviderService", "providerId", provider.getId().toString()),
                "PROVIDER_HEALTH_CHECK_FAILED",
                Map.of("provider", provider.getName(), "error", e.getMessage()),
                null,
                null
            );
            
            return false;
        }
    }

    public List<OpenAIModels.ModelInfo> listModels(ModelProvider provider) {
        String baseUrl = getBaseUrl(provider);
        String apiKey = getApiKey(provider);
        
        try {
            OpenAIModels.ModelsResponse response = restClient.get()
                .uri(baseUrl + "/v1/models")
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .body(OpenAIModels.ModelsResponse.class);
            
            logService.log(
                LogLevel.INFO,
                LogCategory.MODEL,
                Map.of("component", "OpenAIProviderService", "providerId", provider.getId().toString()),
                "PROVIDER_MODELS_LISTED",
                Map.of("provider", provider.getName(), "count", response.data().size()),
                null,
                null
            );
            
            return response.data();
        } catch (Exception e) {
            logService.log(
                LogLevel.ERROR,
                LogCategory.MODEL,
                Map.of("component", "OpenAIProviderService", "providerId", provider.getId().toString()),
                "PROVIDER_MODELS_LIST_FAILED",
                Map.of("provider", provider.getName(), "error", e.getMessage()),
                null,
                null
            );
            
            throw new RuntimeException("Failed to list OpenAI models", e);
        }
    }

    public OpenAIModels.ChatResponse chatCompletion(ModelProvider provider, OpenAIModels.ChatRequest request) {
        String baseUrl = getBaseUrl(provider);
        String apiKey = getApiKey(provider);
        long startTime = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        OpenAIModels.ChatResponse response = null;
        
        try {
            response = restClient.post()
                .uri(baseUrl + "/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(request)
                .retrieve()
                .body(OpenAIModels.ChatResponse.class);
            
            success = true;
            long duration = System.currentTimeMillis() - startTime;
            
            logService.log(
                LogLevel.INFO,
                LogCategory.MODEL,
                Map.of(
                    "component", "OpenAIProviderService",
                    "providerId", provider.getId().toString(),
                    "model", request.model()
                ),
                "CHAT_COMPLETION",
                Map.of(
                    "provider", provider.getName(),
                    "duration_ms", duration,
                    "prompt_tokens", response.usage() != null ? response.usage().promptTokens() : 0,
                    "completion_tokens", response.usage() != null ? response.usage().completionTokens() : 0,
                    "total_tokens", response.usage() != null ? response.usage().totalTokens() : 0
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
                    "component", "OpenAIProviderService",
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
            
            throw new RuntimeException("Failed to complete OpenAI chat", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            usageLogService.logUsage(
                provider,
                request.model(),
                response != null && response.usage() != null ? response.usage().promptTokens() : null,
                response != null && response.usage() != null ? response.usage().completionTokens() : null,
                duration,
                success,
                errorMessage
            );
        }
    }

    private String getBaseUrl(ModelProvider provider) {
        Map<String, Object> config = provider.getConfig();
        if (config == null || !config.containsKey("baseUrl")) {
            return "https://api.openai.com";
        }
        return config.get("baseUrl").toString();
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
