package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record CreateTaskRequest(
    @NotNull(message = "Project ID is required")
    UUID projectId,

    @NotBlank(message = "Title is required")
    String title,

    String assignedAgentId,

    @Pattern(regexp = "^(XS|S|M|L|XL)$", message = "Size must be XS, S, M, L, or XL")
    String size
) {}
