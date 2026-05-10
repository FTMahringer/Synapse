package dev.synapse.core.infrastructure.config;

import dev.synapse.core.dto.SystemMetadataDTO;
import dev.synapse.core.dto.UpdateSystemMetadataRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemMetadataController {

    private final SystemMetadataService metadataService;

    public SystemMetadataController(SystemMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/metadata")
    public SystemMetadataDTO getMetadata() {
        var metadata = metadataService.getMetadata();
        return new SystemMetadataDTO(
            metadata.getName(),
            metadata.getVersion(),
            metadata.getSettings(),
            metadata.getCreatedAt(),
            metadata.getUpdatedAt()
        );
    }

    @PutMapping("/metadata")
    public SystemMetadataDTO updateMetadata(@Valid @RequestBody UpdateSystemMetadataRequest request) {
        var updated = metadataService.updateMetadata(
            request.name(),
            request.version(),
            request.settings()
        );
        return new SystemMetadataDTO(
            updated.getName(),
            updated.getVersion(),
            updated.getSettings(),
            updated.getCreatedAt(),
            updated.getUpdatedAt()
        );
    }

    @GetMapping("/settings")
    public Map<String, Object> getSettings() {
        return metadataService.getSettings();
    }

    @PatchMapping("/settings")
    public Map<String, Object> updateSettings(@RequestBody Map<String, Object> settings) {
        return metadataService.updateSettings(settings);
    }
}
