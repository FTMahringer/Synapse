# MCP Integration

MCP servers expose external tools to agents through the Model Context Protocol using JSON-RPC over stdio or HTTP.

## Transports

| Transport | Use Case |
|---|---|
| `stdio` | Local tool servers launched as child processes |
| `http` | Long-running remote or local MCP servers |

## Main Agent Path

The user runs `/mcp` or `/install` for an MCP plugin. The Main Agent shows tool permissions, startup command, allowed agents, and confirmation before activation.

## Manual Path

Create a manifest under `plugins/mcp/<server-id>/manifest.yml`, declare transport, tools, allowed agents, and confirmation policy, then reload plugins.

## Tool Policy

Default policy is deny. Allowed tools must be listed explicitly. Destructive tools require confirmation.

## Logging

MCP server start, stop, tool call, tool response, failure, and timeout events use `MCP`.
