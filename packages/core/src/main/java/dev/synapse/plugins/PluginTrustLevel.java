package dev.synapse.plugins;

/**
 * Trust level assigned to a plugin based on its store source.
 */
public enum PluginTrustLevel {
    /** Curated and verified by SYNAPSE maintainers. */
    VERIFIED,
    /** Community-submitted, reviewed by pull request. */
    COMMUNITY,
    /** Source unknown or not in store registry. */
    UNVERIFIED
}
