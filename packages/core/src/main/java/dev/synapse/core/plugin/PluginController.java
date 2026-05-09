package dev.synapse.core.plugin;

import dev.synapse.core.dto.DtoMapper;
import dev.synapse.core.dto.PluginDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plugins")
public class PluginController {

    private final PluginLifecycleService lifecycleService;

    public PluginController(PluginLifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @GetMapping
    public List<PluginDTO> listPlugins() {
        return lifecycleService.findAll().stream().map(DtoMapper::toDTO).toList();
    }

    @GetMapping("/{id}")
    public PluginDTO getPlugin(@PathVariable String id) {
        return DtoMapper.toDTO(lifecycleService.findById(id));
    }

    @PostMapping("/install")
    @ResponseStatus(HttpStatus.CREATED)
    public PluginDTO install(@RequestBody Map<String, Object> manifest) {
        return DtoMapper.toDTO(lifecycleService.install(manifest));
    }

    @PostMapping("/{id}/enable")
    public PluginDTO enable(@PathVariable String id) {
        return DtoMapper.toDTO(lifecycleService.enable(id));
    }

    @PostMapping("/{id}/disable")
    public PluginDTO disable(@PathVariable String id) {
        return DtoMapper.toDTO(lifecycleService.disable(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uninstall(@PathVariable String id) {
        lifecycleService.uninstall(id);
    }
}
