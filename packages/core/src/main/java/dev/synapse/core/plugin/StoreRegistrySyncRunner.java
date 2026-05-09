package dev.synapse.core.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Syncs store registry from local file on startup.
 */
@Component
public class StoreRegistrySyncRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StoreRegistrySyncRunner.class);

    private final StoreRegistryService storeRegistryService;

    @Value("${synapse.store.registry-path:store/registry.yml}")
    private String registryPath;

    public StoreRegistrySyncRunner(StoreRegistryService storeRegistryService) {
        this.storeRegistryService = storeRegistryService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            int count = storeRegistryService.syncFromFile(registryPath);
            log.info("Store registry synced: {} entries from {}", count, registryPath);
        } catch (Exception e) {
            log.warn("Store registry sync failed (non-fatal): {}", e.getMessage());
        }
    }
}
