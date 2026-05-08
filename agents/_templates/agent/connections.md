---
last-updated: [YYYY-MM-DD]
---

<!-- INSTRUCTIONS:
  connections.md defines the message routing for this agent.
  It is read by the system router to determine:
    - Who can send tasks/messages to this agent
    - Where this agent sends results and escalations
    - What the agent is hard-blocked from contacting
  Fill every table before deployment. Use agent IDs (kebab-case), not display names.
  "always" / "on-request" / "on-escalation" are the valid trigger values.
-->

## Receives From

<!-- Who sends messages or tasks to this agent, and under what conditions. -->

| Source | Trigger | Priority | Notes |
|--------|---------|----------|-------|
| [agent-id or "user"] | [always \| on-request \| on-escalation] | [high \| medium \| low] | [Optional context] |
| [agent-id or "system"] | [always \| on-request \| on-escalation] | [high \| medium \| low] | [Optional context] |

## Sends To

<!-- Where this agent routes its output, reports, or escalations. -->

| Destination | Condition | Format | Notes |
|-------------|-----------|--------|-------|
| [agent-id or "user"] | [always \| on-completion \| on-error \| on-escalation] | [text \| json \| markdown] | [Optional context] |
| [agent-id or "user"] | [always \| on-completion \| on-error \| on-escalation] | [text \| json \| markdown] | [Optional context] |

## Escalation Path

<!-- What happens when this agent cannot complete a task or hits an error.
     List the chain: first escalation → second escalation → final fallback.
     Be explicit. "Ask user" is a valid final fallback. -->

1. [First escalation target — e.g. "team-leader if task exceeds agent scope"]
2. [Second escalation — e.g. "main-agent if team-leader is unavailable"]
3. [Final fallback — e.g. "Surface error to user with full context"]

## Blocked From

<!-- Hard restrictions on who this agent may never contact or what it may never
     access, regardless of instructions. These are enforced at the router level.
     Examples: "Direct user contact", "External APIs without plugin", "Other
     agents' soul.md files" -->

- [Blocked target or resource 1]
- [Blocked target or resource 2]
