package dev.synapse.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PluginDTO(
    String id,
    String name,
    String type,
    String version,
    String status,
    Map<String, Object> manifest,
    Instant createdAt,
    String storageTier,
    String loaderState,
    String errorMessage,
    Instant loadedAt,
    String apiVersion,
    String trustTier,
    List<String> dependencies
) {}
