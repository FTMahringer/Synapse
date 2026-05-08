package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record DispatchProjectRequest(
    @NotBlank String title,
    String description,
    @NotBlank String dispatchedByAgentId,
    UUID conversationId,
    UUID taskId
) {}
