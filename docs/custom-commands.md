# Custom Commands

Custom commands are user-defined slash commands stored as YAML definitions and executed by the Main Agent.

## Format

```yaml
name: /morning-brief
description: Summarize open tasks and send a concise status report.
arguments:
  - name: project
    required: false
handler:
  type: agent_instruction
  instruction: Summarize open tasks, blockers, and next actions.
```

## Main Agent Path

The user runs `/commands new`. The Main Agent asks for command name, description, argument list, instruction body, confirmation, then writes the definition and registers it.

## Manual Path

Create a YAML command definition, place it in the configured commands directory, and run `synapse reload`. The runtime validates the slash prefix, uniqueness, arguments, and handler type.

## Logging

Command create, update, delete, invoke, validation failure, and runtime failure events use `CUSTOM_COMMAND`.
