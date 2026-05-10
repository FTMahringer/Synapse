package dev.synapse.core;

import dev.synapse.core.infrastructure.config.SynapseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"dev.synapse.core", "dev.synapse.agents", "dev.synapse.conversation", "dev.synapse.tasks", "dev.synapse.users", "dev.synapse.providers"})
@ConfigurationPropertiesScan
@EnableConfigurationProperties(SynapseProperties.class)
@EnableAsync(proxyTargetClass = true)
public class SynapseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SynapseApplication.class, args);
    }
}
