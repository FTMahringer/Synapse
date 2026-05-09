package dev.synapse.core;

import dev.synapse.core.config.SynapseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(SynapseProperties.class)
@EnableAsync
public class SynapseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SynapseApplication.class, args);
    }
}
