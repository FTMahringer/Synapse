package dev.synapse.core.provider.openai;

import dev.synapse.core.domain.ModelProvider;
import dev.synapse.core.provider.ModelProviderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/providers/openai")
public class OpenAIProviderController {

    private final ModelProviderService providerService;
    private final OpenAIProviderService openAIService;

    public OpenAIProviderController(
        ModelProviderService providerService,
        OpenAIProviderService openAIService
    ) {
        this.providerService = providerService;
        this.openAIService = openAIService;
    }

    @GetMapping("/{id}/health")
    public Map<String, Object> checkHealth(@PathVariable UUID id) {
        ModelProvider provider = providerService.findById(id);
        
        if (provider.getType() != ModelProvider.ProviderType.OPENAI && 
            provider.getType() != ModelProvider.ProviderType.OPENAI_COMPATIBLE) {
            throw new IllegalArgumentException("Provider is not an OpenAI-compatible provider");
        }
        
        boolean healthy = openAIService.checkHealth(provider);
        
        return Map.of(
            "providerId", id,
            "providerName", provider.getName(),
            "healthy", healthy,
            "type", provider.getType().name()
        );
    }

    @GetMapping("/{id}/models")
    public Map<String, Object> listModels(@PathVariable UUID id) {
        ModelProvider provider = providerService.findById(id);
        
        if (provider.getType() != ModelProvider.ProviderType.OPENAI && 
            provider.getType() != ModelProvider.ProviderType.OPENAI_COMPATIBLE) {
            throw new IllegalArgumentException("Provider is not an OpenAI-compatible provider");
        }
        
        List<OpenAIModels.ModelInfo> models = openAIService.listModels(provider);
        
        return Map.of(
            "providerId", id,
            "providerName", provider.getName(),
            "models", models,
            "count", models.size()
        );
    }

    @PostMapping("/{id}/chat")
    public OpenAIModels.ChatResponse chat(
        @PathVariable UUID id,
        @Valid @RequestBody OpenAIModels.ChatRequest request
    ) {
        ModelProvider provider = providerService.findById(id);
        
        if (provider.getType() != ModelProvider.ProviderType.OPENAI && 
            provider.getType() != ModelProvider.ProviderType.OPENAI_COMPATIBLE) {
            throw new IllegalArgumentException("Provider is not an OpenAI-compatible provider");
        }
        
        return openAIService.chatCompletion(provider, request);
    }
}
