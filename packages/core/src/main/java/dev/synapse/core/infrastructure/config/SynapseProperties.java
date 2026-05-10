package dev.synapse.core.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "synapse")
public record SynapseProperties(
        @NotBlank String systemName,
        @NotBlank String version,
        @NotBlank String agentsPath,
        Echo echo
) {
    public record Echo(boolean enabled, boolean debugOnly, @NotBlank String activation) {}
}
