package dev.synapse.core.plugin;

import dev.synapse.core.domain.PluginStats;
import dev.synapse.core.repository.PluginStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Tracks install, enable, and disable counts per plugin.
 */
@Service
public class PluginStatsService {

    private final PluginStatsRepository statsRepository;

    public PluginStatsService(PluginStatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Transactional
    public void recordInstall(String pluginId) {
        PluginStats stats = getOrCreate(pluginId);
        stats.setInstallCount(stats.getInstallCount() + 1);
        stats.setLastUsedAt(Instant.now());
        statsRepository.save(stats);
    }

    @Transactional
    public void recordEnable(String pluginId) {
        PluginStats stats = getOrCreate(pluginId);
        stats.setEnableCount(stats.getEnableCount() + 1);
        stats.setLastUsedAt(Instant.now());
        statsRepository.save(stats);
    }

    @Transactional
    public void recordDisable(String pluginId) {
        PluginStats stats = getOrCreate(pluginId);
        stats.setDisableCount(stats.getDisableCount() + 1);
        statsRepository.save(stats);
    }

    @Transactional(readOnly = true)
    public Optional<PluginStats> findById(String pluginId) {
        return statsRepository.findById(pluginId);
    }

    @Transactional(readOnly = true)
    public List<PluginStats> findAll() {
        return statsRepository.findAll();
    }

    private PluginStats getOrCreate(String pluginId) {
        return statsRepository.findById(pluginId).orElseGet(() -> {
            PluginStats s = new PluginStats();
            s.setPluginId(pluginId);
            return s;
        });
    }
}
