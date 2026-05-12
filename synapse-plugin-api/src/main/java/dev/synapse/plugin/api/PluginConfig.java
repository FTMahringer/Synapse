package dev.synapse.plugin.api;

import java.util.Optional;

/**
 * Typed wrapper around manifest config_schema values.
 *
 * Values are validated against the schema before onLoad() is called.
 * Secret fields are injected in clear text within the plugin's isolated ClassLoader —
 * they are never logged by core and must not be logged by the plugin.
 */
public interface PluginConfig {

    /** Returns the string value for {@code key}, or throws if absent and required. */
    String getString(String key);

    /** Returns the string value or the provided default if absent. */
    String getString(String key, String defaultValue);

    /** Returns the boolean value for {@code key}. */
    boolean getBoolean(String key);

    /** Returns the boolean value or the provided default if absent. */
    boolean getBoolean(String key, boolean defaultValue);

    /** Returns the int value for {@code key}. */
    int getInt(String key);

    /** Returns the int value or the provided default if absent. */
    int getInt(String key, int defaultValue);

    /**
     * Returns the secret value for {@code key}.
     * Functionally identical to getString but signals intent — never log this value.
     */
    String getSecret(String key);

    /** Returns an Optional of the secret value — empty if the field is absent or blank. */
    Optional<String> getSecretOptional(String key);

    /** Returns true if the key is present and non-blank. */
    boolean has(String key);
}
