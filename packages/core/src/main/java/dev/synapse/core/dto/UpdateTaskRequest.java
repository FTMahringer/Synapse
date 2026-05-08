package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record UpdateTaskRequest(
    String title,

    @Pattern(regexp = "^(OPEN|IN_PROGRESS|DONE|BLOCKED)$", message = "Status must be OPEN, IN_PROGRESS, DONE, or BLOCKED")
    String status,

    String assignedAgentId,

    @Pattern(regexp = "^(XS|S|M|L|XL)$", message = "Size must be XS, S, M, L, or XL")
    String size
) {}
