package dev.synapse.core.plugin;

import dev.synapse.core.domain.StoreEntry;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final StoreRegistryService storeRegistryService;

    public StoreController(StoreRegistryService storeRegistryService) {
        this.storeRegistryService = storeRegistryService;
    }

    @GetMapping
    public List<StoreEntry> listEntries(
        @RequestParam(required = false) String type
    ) {
        if (type != null) {
            try {
                return storeRegistryService.findByType(StoreEntry.StoreEntryType.valueOf(type.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }
        return storeRegistryService.findAll();
    }
}
