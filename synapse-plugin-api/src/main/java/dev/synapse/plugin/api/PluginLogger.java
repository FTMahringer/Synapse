package dev.synapse.plugin.api;

/**
 * Scoped logger for plugins.
 *
 * All output is tagged with the plugin id and routed through the SYNAPSE system log.
 * Log volume is subject to rate limiting per trust tier.
 * Never log secrets — core does not scrub plugin log output.
 */
public interface PluginLogger {

    void debug(String message);

    void debug(String format, Object... args);

    void info(String message);

    void info(String format, Object... args);

    void warn(String message);

    void warn(String format, Object... args);

    void error(String message);

    void error(String message, Throwable cause);

    void error(String format, Object... args);
}
