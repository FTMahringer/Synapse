package dev.synapse.core.infrastructure.logging;

import dev.synapse.core.infrastructure.config.SynapseProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger {

    private final SystemLogService logs;
    private final SynapseProperties properties;

    public StartupLogger(SystemLogService logs, SynapseProperties properties) {
        this.logs = logs;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logs.info(
                LogCategory.SYSTEM,
                "system.started",
                "{\"component\":\"core\"}",
                "{\"version\":\"%s\",\"system_name\":\"%s\"}".formatted(properties.version(), properties.systemName())
        );
    }
}
