# CLI Reference

The SYNAPSE CLI is a Go command-line application with an optional Bubble Tea TUI for interactive workflows.

## Principles

- Commands are stable and scriptable.
- Interactive flows use Bubble Tea and Huh-style prompts.
- Output defaults to human-readable text.
- `--json` returns machine-readable output where useful.
- Mutating commands show impact and request confirmation unless `--yes` is supplied by an authorized user.

## Global Flags

| Flag | Purpose |
|---|---|
| `--host <url>` | Backend URL |
| `--token <token>` | Auth token |
| `--config <path>` | CLI config path |
| `--json` | JSON output |
| `--yes` | Skip confirmation where allowed |
| `--verbose` | More diagnostic output |

## Commands

| Command | Purpose |
|---|---|
| `synapse chat` | Chat with the Main Agent |
| `synapse chat --agent <id>` | Chat with a specific agent when allowed |
| `synapse agents list` | List agents |
| `synapse agents new` | Guided agent creation |
| `synapse agents inspect <id>` | Show agent detail |
| `synapse teams list` | List teams |
| `synapse teams new` | Guided team creation |
| `synapse firm status` | Show AI-Firm state |
| `synapse firm new` | Create the optional AI-Firm |
| `synapse install <plugin>` | Install plugin from store or path |
| `synapse plugins list` | List installed plugins |
| `synapse channels list` | List channels |
| `synapse models list` | List model providers |
| `synapse store` | Open store TUI |
| `synapse logs` | Tail logs |
| `synapse logs --category <name>` | Filter logs by category |
| `synapse echo` | Manually launch ECHO |
| `synapse config` | View or edit configuration |
| `synapse commands list` | List custom commands |
| `synapse commands new` | Create custom command |
| `synapse git connect <provider>` | Connect git provider |
| `synapse reload` | Reload plugins and agent definitions |

## TUI Views

| View | Behavior |
|---|---|
| Chat | Message stream, agent selector, session status |
| Store | Search, filters, install confirmation |
| Logs | Live tail, category filter, session filter |
| Agents | Agent list, status, model, memory, costs |
| Installer | First-run guided setup when launched locally |

## Main Agent Path

CLI commands route mutating operations through the Main Agent unless they are local diagnostics. The Main Agent shows cost, permissions, and reversible impact before execution.

## Manual Path

Operators can use non-interactive flags and `--json` output for scripts. Manual reload commands validate file-based changes before registering them.

## Logging

CLI command invocation uses the category of the operation being performed. Local CLI failures before backend connection are written to local stderr and are not persisted.
