package dev.synapse.core.repository;

import dev.synapse.core.domain.PluginStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PluginStatsRepository extends JpaRepository<PluginStats, String> {}
