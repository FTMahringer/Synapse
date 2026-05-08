# Heartbeat System

Heartbeat keeps active agent sessions alive and records whether context cache was preserved.

## Behavior

Agents send heartbeat events during active sessions. The default interval is 45 minutes unless a provider requires a shorter keepalive. Heartbeat is disabled for idle agents when `only_when_active` is true.

## Main Agent Path

The user runs `/health` or opens agent settings. The Main Agent reports heartbeat status, last sent time, cache state, and skipped reasons. It can guide configuration changes after confirmation.

## Manual Path

Edit `heartbeat` settings in an agent `config.yml`, then reload agents. Runtime validation ensures interval values are positive and heartbeat is not enabled for ECHO automation.

## Storage

Heartbeat events are stored in `heartbeat_log` with `agent_id`, `session_id`, `sent_at`, and `cache_saved`.

## Logging

Events use `HEARTBEAT`: `heartbeat.sent`, `heartbeat.cache_saved`, `heartbeat.cache_missed`, `heartbeat.skipped`, and `heartbeat.failed`.
