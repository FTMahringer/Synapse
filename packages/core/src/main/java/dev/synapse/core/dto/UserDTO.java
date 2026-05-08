package dev.synapse.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDTO(
    UUID id,
    String username,
    String email,
    String role,
    Map<String, Object> settings,
    Instant createdAt
) {}
