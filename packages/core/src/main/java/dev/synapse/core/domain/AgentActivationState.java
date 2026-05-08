package dev.synapse.core.domain;

/**
 * Runtime activation state for agents.
 * Tracks whether an agent is currently active and available for routing.
 */
public enum AgentActivationState {
    /** Agent is active and available for routing */
    ACTIVE,
    
    /** Agent is temporarily paused (not accepting new requests) */
    PAUSED,
    
    /** Agent is disabled (completely inactive) */
    DISABLED
}
