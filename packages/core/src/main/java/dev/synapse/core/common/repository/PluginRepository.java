package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.core.common.domain.Plugin.PluginType;
import dev.synapse.core.common.domain.Plugin.PluginStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PluginRepository extends JpaRepository<Plugin, String> {
    List<Plugin> findByType(PluginType type);
    List<Plugin> findByStatus(PluginStatus status);
    List<Plugin> findByTypeAndStatus(PluginType type, PluginStatus status);
}
