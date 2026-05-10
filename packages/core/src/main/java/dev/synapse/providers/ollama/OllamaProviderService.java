package dev.synapse.providers.ollama;

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
public class OllamaProviderService {

    private final ModelProviderService providerService;
    private final SystemLogService logService;
    private final ProviderUsageLogService usageLogService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OllamaProviderService(
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
        String baseUrl = getBaseUrl(provider);
        
        try {
            String response = restClient.get()
                .uri(baseUrl + "/api/tags")
                .retrieve()
                .body(String.class);
            
            logService.log(
                LogLevel.INFO,
                LogCategory.MODEL,
                Map.of("component", "OllamaProviderService", "providerId", provider.getId().toString()),
                "PROVIDER_HEALTH_CHECK",
                Map.of("provider", provider.getName(), "status", "healthy"),
                null,
                null
            );
            
            return response != null;
        } catch (Exception e) {
            logService.log(
                LogLevel.WARN,
                LogCategory.MODEL,
                Map.of("component", "OllamaProviderService", "providerId", provider.getId().toString()),
                "PROVIDER_HEALTH_CHECK_FAILED",
                Map.of("provider", provider.getName(), "error", e.getMessage()),
                null,
                null
            );
            
            return false;
        }
    }

    public List<OllamaModels.ModelInfo> listModels(ModelProvider provider) {
        String baseUrl = getBaseUrl(provider);
        
        try {
            OllamaModels.ModelsResponse response = restClient.get()
                .uri(baseUrl + "/api/tags")
                .retrieve()
                .body(OllamaModels.ModelsResponse.class);
            
            logService.log(
                LogLevel.INFO,
                LogCategory.MODEL,
                Map.of("component", "OllamaProviderService", "providerId", provider.getId().toString()),
                "PROVIDER_MODELS_LISTED",
                Map.of("provider", provider.getName(), "count", response.models().size()),
                null,
                null
            );
            
            return response.models();
        } catch (Exception e) {
            logService.log(
                LogLevel.ERROR,
                LogCategory.MODEL,
                Map.of("component", "OllamaProviderService", "providerId", provider.getId().toString()),
                "PROVIDER_MODELS_LIST_FAILED",
                Map.of("provider", provider.getName(), "error", e.getMessage()),
                null,
                null
            );
            
            throw new RuntimeException("Failed to list Ollama models", e);
        }
    }

    public OllamaChat.ChatResponse chatCompletion(ModelProvider provider, OllamaChat.ChatRequest request) {
        String baseUrl = getBaseUrl(provider);
        long startTime = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        OllamaChat.ChatResponse response = null;
        
        try {
            response = restClient.post()
                .uri(baseUrl + "/api/chat")
                .header("Content-Type", "application/json")
                .body(request)
                .retrieve()
                .body(OllamaChat.ChatResponse.class);
            
            success = true;
            long duration = System.currentTimeMillis() - startTime;
            
            logService.log(
                LogLevel.INFO,
                LogCategory.MODEL,
                Map.of(
                    "component", "OllamaProviderService",
                    "providerId", provider.getId().toString(),
                    "model", request.model()
                ),
                "CHAT_COMPLETION",
                Map.of(
                    "provider", provider.getName(),
                    "duration_ms", duration,
                    "prompt_tokens", response.promptEvalCount() != null ? response.promptEvalCount() : 0,
                    "completion_tokens", response.evalCount() != null ? response.evalCount() : 0
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
                    "component", "OllamaProviderService",
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
            
            throw new RuntimeException("Failed to complete Ollama chat", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            usageLogService.logUsage(
                provider,
                request.model(),
                response != null ? response.promptEvalCount() : null,
                response != null ? response.evalCount() : null,
                duration,
                success,
                errorMessage
            );
        }
    }

    private String getBaseUrl(ModelProvider provider) {
        Map<String, Object> config = provider.getConfig();
        if (config == null || !config.containsKey("baseUrl")) {
            return "http://localhost:11434";
        }
        return config.get("baseUrl").toString();
    }
}
