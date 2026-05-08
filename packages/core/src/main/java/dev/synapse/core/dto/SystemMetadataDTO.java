package dev.synapse.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SystemMetadataDTO(
    String name,
    String version,
    Map<String, Object> settings,
    Instant createdAt,
    Instant updatedAt
) {}
