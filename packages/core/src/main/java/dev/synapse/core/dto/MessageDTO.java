package dev.synapse.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageDTO(
    UUID id,
    UUID conversationId,
    String role,
    String content,
    Integer tokens,
    Instant createdAt
) {}
