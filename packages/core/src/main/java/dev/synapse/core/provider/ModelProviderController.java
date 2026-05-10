package dev.synapse.core.provider;

import dev.synapse.core.dto.*;
import dev.synapse.core.provider.ModelProviderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/providers")
public class ModelProviderController {

    private final ModelProviderService providerService;

    public ModelProviderController(ModelProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping
    public List<ModelProviderDTO> listProviders(@RequestParam(required = false) Boolean enabled) {
        var providers = enabled != null && enabled 
            ? providerService.findEnabled() 
            : providerService.findAll();
        
        return providers.stream()
            .map(p -> new ModelProviderDTO(
                p.getId(),
                p.getName(),
                p.getType().name(),
                p.getConfig(),
                p.getEnabled(),
                p.getCreatedAt(),
                p.getUpdatedAt()
            ))
            .toList();
    }

    @GetMapping("/{id}")
    public ModelProviderDTO getProvider(@PathVariable UUID id) {
        var provider = providerService.findById(id);
        return new ModelProviderDTO(
            provider.getId(),
            provider.getName(),
            provider.getType().name(),
            provider.getConfig(),
            provider.getEnabled(),
            provider.getCreatedAt(),
            provider.getUpdatedAt()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModelProviderDTO createProvider(@Valid @RequestBody CreateModelProviderRequest request) {
        var provider = new dev.synapse.core.domain.ModelProvider();
        provider.setName(request.name());
        provider.setType(dev.synapse.core.domain.ModelProvider.ProviderType.valueOf(request.type()));
        provider.setConfig(request.config());
        provider.setEnabled(request.enabled() != null ? request.enabled() : true);
        
        var created = providerService.create(provider, request.secrets());
        
        return new ModelProviderDTO(
            created.getId(),
            created.getName(),
            created.getType().name(),
            created.getConfig(),
            created.getEnabled(),
            created.getCreatedAt(),
            created.getUpdatedAt()
        );
    }

    @PatchMapping("/{id}")
    public ModelProviderDTO updateProvider(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateModelProviderRequest request
    ) {
        var updates = new dev.synapse.core.domain.ModelProvider();
        updates.setName(request.name());
        if (request.type() != null) {
            updates.setType(dev.synapse.core.domain.ModelProvider.ProviderType.valueOf(request.type()));
        }
        updates.setConfig(request.config());
        updates.setEnabled(request.enabled());
        
        var updated = providerService.update(id, updates, request.secrets());
        
        return new ModelProviderDTO(
            updated.getId(),
            updated.getName(),
            updated.getType().name(),
            updated.getConfig(),
            updated.getEnabled(),
            updated.getCreatedAt(),
            updated.getUpdatedAt()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProvider(@PathVariable UUID id) {
        providerService.deleteById(id);
    }
}
