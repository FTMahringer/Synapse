package dev.synapse.core.plugin;

import dev.synapse.core.domain.StoreEntry;
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
    public List<StoreEntry> listEntries(@RequestParam(required = false) String type) {
        if (type != null) {
            try {
                return storeRegistryService.findByType(StoreEntry.StoreEntryType.valueOf(type.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }
        return storeRegistryService.findAll();
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
