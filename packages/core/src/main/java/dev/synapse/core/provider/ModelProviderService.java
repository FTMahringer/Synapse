package dev.synapse.core.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.synapse.core.domain.ModelProvider;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.exception.ValidationException;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.repository.ModelProviderRepository;
import dev.synapse.core.infrastructure.security.SecretEncryptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ModelProviderService {

    private final ModelProviderRepository repository;
    private final SecretEncryptionService encryptionService;
    private final SystemLogService logService;
    private final ObjectMapper objectMapper;

    public ModelProviderService(
        ModelProviderRepository repository,
        SecretEncryptionService encryptionService,
        SystemLogService logService,
        ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.encryptionService = encryptionService;
        this.logService = logService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ModelProvider> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ModelProvider> findEnabled() {
        return repository.findByEnabledTrue();
    }

    @Transactional(readOnly = true)
    public ModelProvider findById(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ModelProvider", id.toString()));
    }

    @Transactional(readOnly = true)
    public ModelProvider findByName(String name) {
        return repository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("ModelProvider", name));
    }

    @Transactional
    public ModelProvider create(ModelProvider provider, Map<String, String> secrets) {
        if (repository.existsByName(provider.getName())) {
            throw new ValidationException("Provider name already exists: " + provider.getName());
        }

        if (secrets != null && !secrets.isEmpty()) {
            String encryptedSecrets = encryptSecrets(secrets);
            provider.setEncryptedSecrets(encryptedSecrets);
        }

        ModelProvider saved = repository.save(provider);

        logService.log(
            LogLevel.INFO,
            LogCategory.MODEL,
            Map.of("component", "ModelProviderService", "providerId", saved.getId().toString()),
            "PROVIDER_CREATED",
            Map.of("name", saved.getName(), "type", saved.getType().name()),
            null,
            null
        );

        return saved;
    }

    @Transactional
    public ModelProvider update(UUID id, ModelProvider updates, Map<String, String> secrets) {
        ModelProvider existing = findById(id);

        boolean changed = false;
        Map<String, Object> changes = new HashMap<>();

        if (updates.getName() != null && !updates.getName().equals(existing.getName())) {
            if (repository.existsByName(updates.getName())) {
                throw new ValidationException("Provider name already exists: " + updates.getName());
            }
            changes.put("name", Map.of("from", existing.getName(), "to", updates.getName()));
            existing.setName(updates.getName());
            changed = true;
        }

        if (updates.getType() != null && updates.getType() != existing.getType()) {
            changes.put("type", Map.of("from", existing.getType().name(), "to", updates.getType().name()));
            existing.setType(updates.getType());
            changed = true;
        }

        if (updates.getConfig() != null) {
            changes.put("config", "updated");
            existing.setConfig(updates.getConfig());
            changed = true;
        }

        if (updates.getEnabled() != null && !updates.getEnabled().equals(existing.getEnabled())) {
            changes.put("enabled", Map.of("from", existing.getEnabled(), "to", updates.getEnabled()));
            existing.setEnabled(updates.getEnabled());
            changed = true;
        }

        if (secrets != null && !secrets.isEmpty()) {
            String encryptedSecrets = encryptSecrets(secrets);
            existing.setEncryptedSecrets(encryptedSecrets);
            changes.put("secrets", "updated");
            changed = true;
        }

        if (!changed) {
            return existing;
        }

        ModelProvider saved = repository.save(existing);

        logService.log(
            LogLevel.INFO,
            LogCategory.MODEL,
            Map.of("component", "ModelProviderService", "providerId", id.toString()),
            "PROVIDER_UPDATED",
            changes,
            null,
            null
        );

        return saved;
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("ModelProvider", id.toString());
        }

        repository.deleteById(id);

        logService.log(
            LogLevel.INFO,
            LogCategory.MODEL,
            Map.of("component", "ModelProviderService", "providerId", id.toString()),
            "PROVIDER_DELETED",
            Map.of(),
            null,
            null
        );
    }

    public Map<String, String> decryptSecrets(ModelProvider provider) {
        if (provider.getEncryptedSecrets() == null) {
            return Map.of();
        }

        try {
            String decrypted = encryptionService.decrypt(provider.getEncryptedSecrets());
            return objectMapper.readValue(decrypted, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse decrypted secrets", e);
        }
    }

    private String encryptSecrets(Map<String, String> secrets) {
        try {
            String json = objectMapper.writeValueAsString(secrets);
            return encryptionService.encrypt(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize secrets", e);
        }
    }
}
