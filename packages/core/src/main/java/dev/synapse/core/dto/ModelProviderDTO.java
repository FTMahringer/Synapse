package dev.synapse.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ModelProviderDTO(
    UUID id,
    String name,
    String type,
    Map<String, Object> config,
    Boolean enabled,
    Instant createdAt,
    Instant updatedAt
) {}
