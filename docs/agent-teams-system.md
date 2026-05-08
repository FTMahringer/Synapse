# Agent Teams System

Agent teams group a leader and member agents behind one routing contract. Teams are optional and unlimited.

## Structure

Each team is described by `team.yml`. The leader has its own `identity.md` and `soul.md`; members are normal agents referenced by ID.

| Field | Meaning |
|---|---|
| `id` | Kebab-case team ID |
| `leader` | Agent ID of the team leader |
| `members` | Agent IDs delegated to by the leader |
| `routing.receives_from` | Agents allowed to send work to the team |
| `routing.reports_to` | Agents that receive completed work |
| `routing.user-direct` | Whether users may address the team directly |

## Main Agent Path

The user runs `/teams new`. The Main Agent asks for domain, leader, members, routing, and user-direct access. It writes `team.yml`, creates or links the leader, validates member IDs, registers the team, and logs `agent_team.created`.

## Manual Path

Create a team folder from `agents/_templates/team/`, fill `team.yml`, create the leader files, and reload agents. Manual teams are accepted only when all referenced agents exist and routing does not bypass the Main Agent unless explicitly enabled.

## Logging

Team create, update, dissolve, task received, task delegated, task blocked, and report submitted events use `AGENT_TEAM`.
