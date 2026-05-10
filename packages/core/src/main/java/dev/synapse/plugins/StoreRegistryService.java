package dev.synapse.plugins;

import dev.synapse.core.common.domain.StoreEntry;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.common.repository.StoreEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Syncs store entries from local store/registry.yml into the database.
 */
@Service
public class StoreRegistryService {

    private static final Logger log = LoggerFactory.getLogger(StoreRegistryService.class);

    private final StoreEntryRepository storeEntryRepository;
    private final SystemLogService logService;

    public StoreRegistryService(StoreEntryRepository storeEntryRepository, SystemLogService logService) {
        this.storeEntryRepository = storeEntryRepository;
        this.logService = logService;
    }

    @Transactional
    public int syncFromFile(String registryPath) {
        Map<String, Object> registry = loadYaml(registryPath);
        if (registry == null) return 0;

        List<StoreEntry> entries = new ArrayList<>();
        entries.addAll(parsePlugins(registry));
        entries.addAll(parseBundles(registry));

        storeEntryRepository.saveAll(entries);

        logService.log(LogLevel.INFO, LogCategory.STORE,
            Map.of("component", "StoreRegistryService"),
            "STORE_SYNCED",
            Map.of("count", entries.size(), "path", registryPath),
            null, null);

        return entries.size();
    }

    @Transactional(readOnly = true)
    public List<StoreEntry> findAll() {
        return storeEntryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StoreEntry> findByType(StoreEntry.StoreEntryType type) {
        return storeEntryRepository.findByType(type);
    }

    @SuppressWarnings("unchecked")
    private List<StoreEntry> parsePlugins(Map<String, Object> registry) {
        Object pluginsObj = registry.get("plugins");
        if (!(pluginsObj instanceof List<?> list)) return List.of();

        List<StoreEntry> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> raw)) continue;
            Map<String, Object> map = (Map<String, Object>) raw;
            StoreEntry entry = mapToEntry(map, StoreEntry.StoreEntryType.PLUGIN);
            if (entry != null) result.add(entry);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<StoreEntry> parseBundles(Map<String, Object> registry) {
        Object bundlesObj = registry.get("bundles");
        if (!(bundlesObj instanceof List<?> list)) return List.of();

        List<StoreEntry> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> raw)) continue;
            Map<String, Object> map = (Map<String, Object>) raw;
            StoreEntry entry = mapToEntry(map, StoreEntry.StoreEntryType.BUNDLE);
            if (entry != null) result.add(entry);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private StoreEntry mapToEntry(Map<String, Object> map, StoreEntry.StoreEntryType type) {
        String id = str(map, "id");
        if (id == null) return null;

        StoreEntry entry = new StoreEntry();
        entry.setId(id);
        entry.setName(str(map, "name") != null ? str(map, "name") : id);
        entry.setType(type);
        entry.setSource(str(map, "source") != null ? str(map, "source") : "unknown");
        entry.setVersion(str(map, "version") != null ? str(map, "version") : "0.0.0");
        entry.setAuthor(str(map, "author"));
        entry.setLicense(str(map, "license"));
        entry.setDescription(str(map, "description"));
        entry.setMinSynapse(str(map, "min_synapse"));

        Object tagsObj = map.get("tags");
        if (tagsObj instanceof List<?> tagList) {
            entry.setTags(tagList.stream().map(Object::toString).toList());
        }
        entry.setMeta(map);
        return entry;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYaml(String path) {
        Yaml yaml = new Yaml();
        try {
            File file = new File(path);
            if (file.exists()) {
                try (InputStream is = new FileInputStream(file)) {
                    return yaml.load(is);
                }
            }
            // fallback: classpath
            ClassPathResource resource = new ClassPathResource("store/registry.yml");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    return yaml.load(is);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to load registry YAML from {}: {}", path, e.getMessage());
        }
        return null;
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
