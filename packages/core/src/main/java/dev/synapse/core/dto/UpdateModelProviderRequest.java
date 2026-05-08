package dev.synapse.core.dto;

import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record UpdateModelProviderRequest(
    String name,
    
    @Pattern(regexp = "^(OLLAMA|OPENAI|ANTHROPIC|OPENAI_COMPATIBLE)$", 
             message = "Type must be OLLAMA, OPENAI, ANTHROPIC, or OPENAI_COMPATIBLE")
    String type,
    
    Map<String, Object> config,
    Map<String, String> secrets,
    Boolean enabled
) {}
