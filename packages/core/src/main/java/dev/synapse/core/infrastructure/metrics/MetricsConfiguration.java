package dev.synapse.core.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class MetricsConfiguration {

    @Autowired
    private MeterRegistry registry;

    @PostConstruct
    public void configureMeterRegistry() {
        registry.config().commonTags(
                Collections.singletonList(Tag.of("service", "synapse-core"))
        );
    }
}
