package dev.synapse.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record CreateModelProviderRequest(
    @NotBlank(message = "Name is required")
    String name,
    
    @NotNull(message = "Type is required")
    @Pattern(regexp = "^(OLLAMA|OPENAI|ANTHROPIC|OPENAI_COMPATIBLE)$", 
             message = "Type must be OLLAMA, OPENAI, ANTHROPIC, or OPENAI_COMPATIBLE")
    String type,
    
    Map<String, Object> config,
    Map<String, String> secrets,
    Boolean enabled
) {}
