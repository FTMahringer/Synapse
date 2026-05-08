package dev.synapse.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentDTO(
    String id,
    String name,
    String type,
    String status,
    Map<String, Object> config,
    Instant createdAt
) {}
