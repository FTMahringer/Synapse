package dev.synapse.core.infrastructure.config;

import dev.synapse.core.domain.SystemMetadata;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.repository.SystemMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class SystemMetadataService {

    private final SystemMetadataRepository metadataRepository;
    private final SystemLogService logService;

    public SystemMetadataService(
        SystemMetadataRepository metadataRepository,
        SystemLogService logService
    ) {
        this.metadataRepository = metadataRepository;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public SystemMetadata getMetadata() {
        return metadataRepository.findById(true)
            .orElseGet(this::createDefault);
    }

    @Transactional
    public SystemMetadata updateMetadata(String name, String version, Map<String, Object> settings) {
        SystemMetadata metadata = metadataRepository.findById(true)
            .orElseGet(this::createDefault);

        if (name != null) {
            metadata.setName(name);
        }
        if (version != null) {
            metadata.setVersion(version);
        }
        if (settings != null) {
            metadata.setSettings(settings);
        }

        SystemMetadata saved = metadataRepository.save(metadata);

        logService.log(
            LogLevel.INFO,
            LogCategory.SYSTEM,
            Map.of("component", "SystemMetadataService"),
            "METADATA_UPDATED",
            Map.of("version", saved.getVersion()),
            null,
            null
        );

        return saved;
    }

    @Transactional
    public Map<String, Object> getSettings() {
        return getMetadata().getSettings();
    }

    @Transactional
    public Map<String, Object> updateSettings(Map<String, Object> settings) {
        SystemMetadata metadata = getMetadata();
        
        Map<String, Object> merged = new java.util.HashMap<>(metadata.getSettings());
        merged.putAll(settings);
        
        metadata.setSettings(merged);
        SystemMetadata saved = metadataRepository.save(metadata);

        logService.log(
            LogLevel.INFO,
            LogCategory.SYSTEM,
            Map.of("component", "SystemMetadataService"),
            "SETTINGS_UPDATED",
            Map.of("keys", settings.keySet()),
            null,
            null
        );

        return saved.getSettings();
    }

    private SystemMetadata createDefault() {
        SystemMetadata metadata = new SystemMetadata();
        metadata.setName("SYNAPSE");
        metadata.setVersion("1.0.0");
        metadata.setSettings(Map.of());
        return metadata;
    }
}
