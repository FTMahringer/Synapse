package dev.synapse.core.repository;

import dev.synapse.core.domain.Plugin;
import dev.synapse.core.domain.Plugin.PluginType;
import dev.synapse.core.domain.Plugin.PluginStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PluginRepository extends JpaRepository<Plugin, String> {
    List<Plugin> findByType(PluginType type);
    List<Plugin> findByStatus(PluginStatus status);
    List<Plugin> findByTypeAndStatus(PluginType type, PluginStatus status);
}
