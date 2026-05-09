# SYNAPSE CLI

Go CLI for the SYNAPSE AI platform.

## Build

```bash
go build -o synapse .
```

Requires Go 1.22+.

## Usage

```
synapse [command] [flags]

Commands:
  auth        Authentication (login, logout, session)
  health      Check backend health
  agents      Manage agents (list, runtime, activate, pause)
  providers   Manage model providers (list, test)
  plugins     Manage plugins (list, enable, disable)
  store       Browse store registry (list)
  chat        Conversations (list, new, send, interactive, messages)
  logs        View system logs (list, stream)
  config      Manage CLI profiles (list, set, show)
  tui         Launch interactive TUI dashboard

Global Flags:
  -p, --profile string   Config profile (default: "default")
  -H, --host string      Backend host override
  -j, --json             Output raw JSON

Examples:
  synapse config set default http://localhost:8080
  synapse auth login -u admin -w secret
  synapse health
  synapse agents list
  synapse chat new
  synapse chat send <convId> "Hello, agent"
  synapse chat interactive <convId>
  synapse tui
```

## Config

Config stored at `~/.synapse/config.yaml`:

```yaml
profiles:
  default:
    host: http://localhost:8080
    token: <jwt-token>
```
