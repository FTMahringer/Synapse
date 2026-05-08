package dev.synapse.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskLogDTO(
    UUID id,
    UUID taskId,
    String event,
    Map<String, Object> payload,
    Instant createdAt
) {}
