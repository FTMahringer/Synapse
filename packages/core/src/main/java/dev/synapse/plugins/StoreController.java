package dev.synapse.plugins;

import dev.synapse.core.common.domain.StoreEntry;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final StoreRegistryService storeRegistryService;
    private final BundleInstallService bundleInstallService;

    public StoreController(
        StoreRegistryService storeRegistryService,
        BundleInstallService bundleInstallService
    ) {
        this.storeRegistryService = storeRegistryService;
        this.bundleInstallService = bundleInstallService;
    }

    @GetMapping
    public List<StoreEntry> listEntries(
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        if (type != null) {
            try {
                return storeRegistryService.findByType(
                    StoreEntry.StoreEntryType.valueOf(type.toUpperCase()),
                    page,
                    size
                );
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }
        return storeRegistryService.findAll(page, size);
    }

    @GetMapping("/{id}")
    public StoreEntry getEntry(@PathVariable String id) {
        StoreEntry entry = storeRegistryService.findById(id);
        if (entry == null) {
            throw new dev.synapse.core.infrastructure.exception.ResourceNotFoundException(
                "StoreEntry",
                id
            );
        }
        return entry;
    }

    @PostMapping("/{id}/validate")
    public ValidationResult validateBundle(@PathVariable String id) {
        return bundleInstallService.validateBundle(id);
    }

    @PostMapping("/{id}/install")
    public BundleInstallService.BundleInstallResult installBundle(
        @PathVariable String id
    ) {
        return bundleInstallService.installBundle(id);
    }
}
