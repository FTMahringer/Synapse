package dev.synapse.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
    String username,
    
    @Email(message = "Invalid email format")
    String email,
    
    @Pattern(regexp = "^(OWNER|ADMIN|USER|VIEWER)$", message = "Role must be OWNER, ADMIN, USER, or VIEWER")
    String role
) {}
