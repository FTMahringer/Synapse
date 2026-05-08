package dev.synapse.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskDTO(
    UUID id,
    UUID projectId,
    String title,
    String status,
    String assignedAgentId,
    String size,
    Integer version,
    Instant createdAt
) {}
