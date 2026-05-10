package dev.synapse.plugins;

import dev.synapse.core.common.domain.StoreEntry;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final StoreRegistryService storeRegistryService;
    private final BundleInstallService bundleInstallService;

    public StoreController(StoreRegistryService storeRegistryService, BundleInstallService bundleInstallService) {
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
                return storeRegistryService.findByType(StoreEntry.StoreEntryType.valueOf(type.toUpperCase()), page, size);
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }
        return storeRegistryService.findAll(page, size);
    }

    @PostMapping("/{id}/validate")
    public ValidationResult validateBundle(@PathVariable String id) {
        return bundleInstallService.validateBundle(id);
    }

    @PostMapping("/{id}/install")
    public BundleInstallService.BundleInstallResult installBundle(@PathVariable String id) {
        return bundleInstallService.installBundle(id);
    }
}
