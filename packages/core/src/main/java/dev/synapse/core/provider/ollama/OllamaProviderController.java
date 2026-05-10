package dev.synapse.core.provider.ollama;

import dev.synapse.core.common.domain.ModelProvider;
import dev.synapse.core.provider.ModelProviderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/providers/ollama")
public class OllamaProviderController {

    private final ModelProviderService providerService;
    private final OllamaProviderService ollamaService;

    public OllamaProviderController(
        ModelProviderService providerService,
        OllamaProviderService ollamaService
    ) {
        this.providerService = providerService;
        this.ollamaService = ollamaService;
    }

    @GetMapping("/{id}/health")
    public Map<String, Object> checkHealth(@PathVariable UUID id) {
        ModelProvider provider = providerService.findById(id);
        
        if (provider.getType() != ModelProvider.ProviderType.OLLAMA) {
            throw new IllegalArgumentException("Provider is not an Ollama provider");
        }
        
        boolean healthy = ollamaService.checkHealth(provider);
        
        return Map.of(
            "providerId", id,
            "providerName", provider.getName(),
            "healthy", healthy,
            "type", "OLLAMA"
        );
    }

    @GetMapping("/{id}/models")
    public Map<String, Object> listModels(@PathVariable UUID id) {
        ModelProvider provider = providerService.findById(id);
        
        if (provider.getType() != ModelProvider.ProviderType.OLLAMA) {
            throw new IllegalArgumentException("Provider is not an Ollama provider");
        }
        
        List<OllamaModels.ModelInfo> models = ollamaService.listModels(provider);
        
        return Map.of(
            "providerId", id,
            "providerName", provider.getName(),
            "models", models,
            "count", models.size()
        );
    }

    @PostMapping("/{id}/chat")
    public OllamaChat.ChatResponse chat(
        @PathVariable UUID id,
        @Valid @RequestBody OllamaChat.ChatRequest request
    ) {
        ModelProvider provider = providerService.findById(id);
        
        if (provider.getType() != ModelProvider.ProviderType.OLLAMA) {
            throw new IllegalArgumentException("Provider is not an Ollama provider");
        }
        
        return ollamaService.chatCompletion(provider, request);
    }
}
