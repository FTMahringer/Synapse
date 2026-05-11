package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record SendCollaborationMessageRequest(
    @NotBlank String fromAgentId,
    @NotBlank String toAgentId,
    @NotBlank String messageType,
    @NotBlank String content,
    Map<String, Object> metadata
) {}
