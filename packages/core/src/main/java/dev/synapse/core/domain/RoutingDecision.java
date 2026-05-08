package dev.synapse.core.domain;

/**
 * Routing decision made by Main Agent.
 * Tracks where a request was routed and why.
 */
public enum RoutingDecision {
    /** Handled directly by Main Agent */
    HANDLE_DIRECTLY,
    
    /** Routed to a team leader */
    ROUTE_TO_TEAM_LEADER,
    
    /** Routed to a team member */
    ROUTE_TO_TEAM_MEMBER,
    
    /** Routed to AI-Firm project */
    ROUTE_TO_FIRM_PROJECT,
    
    /** Rejected (no suitable handler) */
    REJECTED
}
