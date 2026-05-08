package dev.synapse.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record CreateUserRequest(
    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,32}$", message = "Username must be 3-32 characters, alphanumeric with - or _")
    String username,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(OWNER|ADMIN|USER|VIEWER)$", message = "Role must be OWNER, ADMIN, USER, or VIEWER")
    String role,

    Map<String, Object> settings
) {}
