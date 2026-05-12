package dev.synapse.plugins.loader;

import java.util.List;

/**
 * Parsed plugin dependency declaration from manifest.
 *
 * <p>Manifest format:
 * <pre>
 * requires:
 *   plugins:
 *     - id: telegram-channel
 *       version: ">=1.0.0"
 *     - id: openai-provider
 *       version: "^2.0.0"
 *   soft_requires:
 *     - id: image-generation-skill
 *       version: ">=0.5.0"
 * </pre>
 */
public record PluginDependency(
    String id,
    String versionSpec,
    boolean soft
) {

    /**
     * Parses a dependency map from the manifest.
     */
    @SuppressWarnings("unchecked")
    public static PluginDependency fromMap(java.util.Map<String, Object> map, boolean soft) {
        String id = map.get("id") != null ? map.get("id").toString() : null;
        String version = map.get("version") != null ? map.get("version").toString() : "*";
        return new PluginDependency(id, version, soft);
    }

    /**
     * Extracts dependency list from a manifest map.
     */
    @SuppressWarnings("unchecked")
    public static List<PluginDependency> fromManifest(java.util.Map<String, Object> manifest) {
        java.util.List<PluginDependency> deps = new java.util.ArrayList<>();

        Object requires = manifest.get("requires");
        if (requires instanceof java.util.Map<?, ?> reqMap) {
            Object plugins = reqMap.get("plugins");
            if (plugins instanceof java.util.List<?> list) {
                for (Object item : list) {
                    if (item instanceof java.util.Map<?, ?> m) {
                        deps.add(PluginDependency.fromMap((java.util.Map<String, Object>) m, false));
                    }
                }
            }
            Object softReq = reqMap.get("soft_requires");
            if (softReq instanceof java.util.List<?> list) {
                for (Object item : list) {
                    if (item instanceof java.util.Map<?, ?> m) {
                        deps.add(PluginDependency.fromMap((java.util.Map<String, Object>) m, true));
                    }
                }
            }
        }

        return deps;
    }
}
