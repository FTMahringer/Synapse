package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateDelegationRequest(
    UUID taskId,
    @NotBlank String fromAgentId,
    @NotBlank String toAgentId,
    String delegationNote
) {}
