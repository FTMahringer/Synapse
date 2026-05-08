package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordRequest(
    @NotBlank(message = "New password is required")
    String newPassword
) {}
