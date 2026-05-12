package dev.synapse.plugins;

import dev.synapse.core.common.domain.Plugin;
import dev.synapse.plugins.loader.PluginDependency;
import java.util.List;
import java.util.Map;

/**
 * Parsed plugin manifest. Validated before install.
 */
public record PluginManifest(
    String id,
    String name,
    Plugin.PluginType type,
    String version,
    String author,
    String license,
    String description,
    String minSynapse,
    List<String> tags,
    List<PluginDependency> dependencies,
    List<PluginDependency> softDependencies,
    Map<String, Object> raw
) {
    public static PluginManifest fromMap(Map<String, Object> map) {
        String typeStr = str(map, "type");
        Plugin.PluginType type = null;
        if (typeStr != null) {
            try {
                type = Plugin.PluginType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Object tagsObj = map.get("tags");
        List<String> tags =
            tagsObj instanceof List<?> list
                ? list.stream().map(Object::toString).toList()
                : List.of();

        List<PluginDependency> deps = PluginDependency.fromManifest(map);
        List<PluginDependency> hardDeps = deps
            .stream()
            .filter(d -> !d.soft())
            .toList();
        List<PluginDependency> softDeps = deps
            .stream()
            .filter(d -> d.soft())
            .toList();

        return new PluginManifest(
            str(map, "id"),
            str(map, "name"),
            type,
            str(map, "version"),
            str(map, "author"),
            str(map, "license"),
            str(map, "description"),
            str(map, "min_synapse"),
            tags,
            hardDeps,
            softDeps,
            map
        );
    }

    private static String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
