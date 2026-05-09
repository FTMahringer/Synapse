package dev.synapse.core.plugin;

import dev.synapse.core.domain.Plugin;

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
        List<String> tags = tagsObj instanceof List<?> list
            ? list.stream().map(Object::toString).toList()
            : List.of();

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
            map
        );
    }

    private static String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
