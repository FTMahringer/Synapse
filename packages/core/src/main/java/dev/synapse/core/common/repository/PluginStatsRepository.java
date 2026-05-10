package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.PluginStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PluginStatsRepository extends JpaRepository<PluginStats, String> {}
