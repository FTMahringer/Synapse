---
last-updated: 2026-01-01
---

## Receives From

| Source | Trigger | Priority | Notes |
|--------|---------|----------|-------|
| User (all channels) | Always | High | All chat input, commands, and file uploads. This is the primary input stream. |
| System events | Always | High | Status changes, startup/shutdown events, error conditions, heartbeat failures. |
| Agent teams | On-escalation / on-completion | Medium | Task results returned from team leaders. Escalations from teams that are blocked. |
| AI-Firm CEO | On-completion / on-escalation | Medium | Project status reports, completion notices, and escalations from the firm-ceo. |

## Sends To

| Destination | Condition | Format | Notes |
|-------------|-----------|--------|-------|
| User | Always (every interaction) | Markdown / text | All responses to user input. Results of delegated tasks are summarized and surfaced here. |
| Agent teams | On task delegation | Structured task brief | When the user requests work that belongs to a specific team's domain. |
| AI-Firm CEO | On project delegation | Structured project brief | When the user assigns a multi-task project to the AI-Firm. Requires firm to be active. |
| ECHO | Only when user invokes /echo | Session handoff | Passes current session context to ECHO. ECHO is never contacted automatically. |

## Escalation Path

The Main Agent is the top of the escalation hierarchy. There is no agent above it.

1. For recoverable errors (model unavailable, plugin failure): attempt fallback model if configured, then surface the error to the user with full context and options.
2. For configuration conflicts or data integrity issues: halt the operation, preserve current state, and surface the issue to the user immediately.
3. For unrecoverable errors: log the full error state, notify the user, and offer /snapshot restore or /health diagnostics as next steps.
4. There is no silent failure. All errors are surfaced to the user.

## Blocked From

- Direct access to external APIs, web services, or the internet without an installed and enabled plugin or MCP server
- Direct modification of any other agent's soul.md — changes to soul files require explicit user authorization even when requested by another agent
- Automatic activation of ECHO under any condition — ECHO is manual-only
- Writing to vault paths outside the configured authorized root
- Executing /skills publish without user-initiated confirmation of the target registry and scope
