package dev.synapse.core.provider.anthropic;

import dev.synapse.core.domain.ModelProvider;
import dev.synapse.core.service.ModelProviderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/providers/anthropic")
public class AnthropicProviderController {

    private final ModelProviderService providerService;
    private final AnthropicProviderService anthropicService;

    public AnthropicProviderController(
        ModelProviderService providerService,
        AnthropicProviderService anthropicService
    ) {
        this.providerService = providerService;
        this.anthropicService = anthropicService;
    }

    @GetMapping("/{id}/health")
    public Map<String, Object> checkHealth(@PathVariable UUID id) {
        ModelProvider provider = providerService.findById(id);
        
        if (provider.getType() != ModelProvider.ProviderType.ANTHROPIC) {
            throw new IllegalArgumentException("Provider is not an Anthropic provider");
        }
        
        boolean healthy = anthropicService.checkHealth(provider);
        
        return Map.of(
            "providerId", id,
            "providerName", provider.getName(),
            "healthy", healthy,
            "type", "ANTHROPIC"
        );
    }

    @PostMapping("/{id}/chat")
    public AnthropicModels.ChatResponse chat(
        @PathVariable UUID id,
        @Valid @RequestBody AnthropicModels.ChatRequest request
    ) {
        ModelProvider provider = providerService.findById(id);
        
        if (provider.getType() != ModelProvider.ProviderType.ANTHROPIC) {
            throw new IllegalArgumentException("Provider is not an Anthropic provider");
        }
        
        return anthropicService.chatCompletion(provider, request);
    }
}
