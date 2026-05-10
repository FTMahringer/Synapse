package dev.synapse.core.plugin;

import dev.synapse.core.domain.Plugin;
import dev.synapse.core.exception.ResourceNotFoundException;
import dev.synapse.core.repository.PluginRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PluginService {

    private final PluginRepository pluginRepository;

    public PluginService(PluginRepository pluginRepository) {
        this.pluginRepository = pluginRepository;
    }

    @Transactional(readOnly = true)
    public List<Plugin> findAll() {
        return pluginRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Plugin findById(String id) {
        return pluginRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plugin", id));
    }

    @Transactional
    public Plugin save(Plugin plugin) {
        return pluginRepository.save(plugin);
    }

    @Transactional
    public void deleteById(String id) {
        if (!pluginRepository.existsById(id)) {
            throw new ResourceNotFoundException("Plugin", id);
        }
        pluginRepository.deleteById(id);
    }
}
