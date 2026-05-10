package dev.synapse.plugins;

import java.util.List;

/**
 * Safety assessment for a plugin install.
 * Community and unverified plugins require operator confirmation.
 */
public record PluginSafetyPolicy(
    PluginTrustLevel trustLevel,
    boolean requiresConfirmation,
    List<String> warnings
) {
    public static PluginSafetyPolicy verified() {
        return new PluginSafetyPolicy(PluginTrustLevel.VERIFIED, false, List.of());
    }

    public static PluginSafetyPolicy community(String source) {
        return new PluginSafetyPolicy(
            PluginTrustLevel.COMMUNITY,
            true,
            List.of(
                "This plugin is from the community source '" + source + "' and has not been verified by SYNAPSE maintainers.",
                "Review the plugin manifest and source before installing.",
                "Pass confirmed=true to proceed."
            )
        );
    }

    public static PluginSafetyPolicy unverified(String source) {
        return new PluginSafetyPolicy(
            PluginTrustLevel.UNVERIFIED,
            true,
            List.of(
                "This plugin source '" + source + "' is not in the store registry.",
                "Install only from sources you trust.",
                "Pass confirmed=true to proceed."
            )
        );
    }
}
