package dev.synapse.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PluginDTO(
    String id,
    String name,
    String type,
    String version,
    String status,
    Map<String, Object> manifest,
    Instant createdAt
) {}
