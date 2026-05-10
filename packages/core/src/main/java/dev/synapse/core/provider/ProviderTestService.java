package dev.synapse.core.provider;

import dev.synapse.core.domain.ModelProvider;
import dev.synapse.core.dto.TestProviderRequest;
import dev.synapse.core.dto.TestProviderResponse;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.provider.anthropic.AnthropicModels;
import dev.synapse.core.provider.anthropic.AnthropicProviderService;
import dev.synapse.core.provider.ollama.OllamaChat;
import dev.synapse.core.provider.ollama.OllamaProviderService;
import dev.synapse.core.provider.openai.OpenAIModels;
import dev.synapse.core.provider.openai.OpenAIProviderService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProviderTestService {

    private final ModelProviderService providerService;
    private final OllamaProviderService ollamaService;
    private final OpenAIProviderService openAIService;
    private final AnthropicProviderService anthropicService;
    private final SystemLogService logService;

    public ProviderTestService(
        ModelProviderService providerService,
        OllamaProviderService ollamaService,
        OpenAIProviderService openAIService,
        AnthropicProviderService anthropicService,
        SystemLogService logService
    ) {
        this.providerService = providerService;
        this.ollamaService = ollamaService;
        this.openAIService = openAIService;
        this.anthropicService = anthropicService;
        this.logService = logService;
    }

    public TestProviderResponse testProvider(TestProviderRequest request) {
        ModelProvider provider = providerService.findById(request.providerId());
        long startTime = System.currentTimeMillis();
        
        boolean storePrompt = request.storePrompt() != null ? request.storePrompt() : false;
        
        if (!storePrompt) {
            logService.log(
                LogLevel.INFO,
                LogCategory.MODEL,
                Map.of("component", "ProviderTestService", "providerId", provider.getId().toString()),
                "PROVIDER_TEST_STARTED",
                Map.of(
                    "provider", provider.getName(),
                    "model", request.model(),
                    "storePrompt", false
                ),
                null,
                null
            );
        }
        
        try {
            return switch (provider.getType()) {
                case OLLAMA -> testOllama(provider, request, startTime);
                case OPENAI, OPENAI_COMPATIBLE -> testOpenAI(provider, request, startTime);
                case ANTHROPIC -> testAnthropic(provider, request, startTime);
                default -> throw new IllegalArgumentException("Unsupported provider type: " + provider.getType());
            };
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            
            logService.log(
                LogLevel.ERROR,
                LogCategory.MODEL,
                Map.of("component", "ProviderTestService", "providerId", provider.getId().toString()),
                "PROVIDER_TEST_FAILED",
                Map.of(
                    "provider", provider.getName(),
                    "model", request.model(),
                    "error", e.getMessage(),
                    "latency_ms", latency
                ),
                null,
                null
            );
            
            return new TestProviderResponse(
                false,
                provider.getName(),
                provider.getType().name(),
                request.model(),
                latency,
                null,
                null,
                null,
                null,
                e.getMessage(),
                Map.of("storePrompt", storePrompt)
            );
        }
    }

    private TestProviderResponse testOllama(ModelProvider provider, TestProviderRequest request, long startTime) {
        OllamaChat.ChatRequest ollamaRequest = new OllamaChat.ChatRequest(
            request.model(),
            request.messages().stream()
                .map(m -> new OllamaChat.Message(m.role(), m.content()))
                .toList(),
            false,
            new OllamaChat.Options(
                request.temperature(),
                null,
                null,
                request.maxTokens()
            )
        );
        
        OllamaChat.ChatResponse response = ollamaService.chatCompletion(provider, ollamaRequest);
        long latency = System.currentTimeMillis() - startTime;
        
        String preview = response.message() != null && response.message().content() != null
            ? response.message().content().substring(0, Math.min(200, response.message().content().length()))
            : null;
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("storePrompt", request.storePrompt() != null ? request.storePrompt() : false);
        metadata.put("totalDuration", response.totalDuration());
        metadata.put("loadDuration", response.loadDuration());
        
        return new TestProviderResponse(
            true,
            provider.getName(),
            provider.getType().name(),
            request.model(),
            latency,
            response.promptEvalCount(),
            response.evalCount(),
            (response.promptEvalCount() != null ? response.promptEvalCount() : 0) + 
                (response.evalCount() != null ? response.evalCount() : 0),
            preview,
            null,
            metadata
        );
    }

    private TestProviderResponse testOpenAI(ModelProvider provider, TestProviderRequest request, long startTime) {
        OpenAIModels.ChatRequest openAIRequest = new OpenAIModels.ChatRequest(
            request.model(),
            request.messages().stream()
                .map(m -> new OpenAIModels.Message(m.role(), m.content()))
                .toList(),
            request.temperature(),
            request.maxTokens(),
            null,
            null,
            null,
            false
        );
        
        OpenAIModels.ChatResponse response = openAIService.chatCompletion(provider, openAIRequest);
        long latency = System.currentTimeMillis() - startTime;
        
        String preview = null;
        if (response.choices() != null && !response.choices().isEmpty()) {
            OpenAIModels.Message message = response.choices().get(0).message();
            if (message != null && message.content() != null) {
                preview = message.content().substring(0, Math.min(200, message.content().length()));
            }
        }
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("storePrompt", request.storePrompt() != null ? request.storePrompt() : false);
        metadata.put("responseId", response.id());
        metadata.put("finishReason", response.choices() != null && !response.choices().isEmpty() 
            ? response.choices().get(0).finishReason() 
            : null);
        
        return new TestProviderResponse(
            true,
            provider.getName(),
            provider.getType().name(),
            request.model(),
            latency,
            response.usage() != null ? response.usage().promptTokens() : null,
            response.usage() != null ? response.usage().completionTokens() : null,
            response.usage() != null ? response.usage().totalTokens() : null,
            preview,
            null,
            metadata
        );
    }

    private TestProviderResponse testAnthropic(ModelProvider provider, TestProviderRequest request, long startTime) {
        AnthropicModels.ChatRequest anthropicRequest = new AnthropicModels.ChatRequest(
            request.model(),
            request.maxTokens() != null ? request.maxTokens() : 1024,
            request.messages().stream()
                .map(m -> new AnthropicModels.Message(m.role(), m.content()))
                .toList(),
            request.temperature(),
            null,
            null,
            false
        );
        
        AnthropicModels.ChatResponse response = anthropicService.chatCompletion(provider, anthropicRequest);
        long latency = System.currentTimeMillis() - startTime;
        
        String preview = null;
        if (response.content() != null && !response.content().isEmpty()) {
            AnthropicModels.ContentBlock block = response.content().get(0);
            if (block.text() != null) {
                preview = block.text().substring(0, Math.min(200, block.text().length()));
            }
        }
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("storePrompt", request.storePrompt() != null ? request.storePrompt() : false);
        metadata.put("responseId", response.id());
        metadata.put("stopReason", response.stopReason());
        
        return new TestProviderResponse(
            true,
            provider.getName(),
            provider.getType().name(),
            request.model(),
            latency,
            response.usage() != null ? response.usage().inputTokens() : null,
            response.usage() != null ? response.usage().outputTokens() : null,
            (response.usage() != null ? response.usage().inputTokens() : 0) + 
                (response.usage() != null ? response.usage().outputTokens() : 0),
            preview,
            null,
            metadata
        );
    }
}
