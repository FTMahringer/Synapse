# v2.3.6-dev Native Java Tool Integration Design

## Problem Statement

`v2.3.6-dev` is the foundation step for the Java-first plugin system (`v2.6.0` track). Today, tooling is not yet modeled as a first-class native Java execution framework with clear contracts for discovery, execution, and caching.

Without a stable tool foundation, adding future Java tools (or plugin-exposed tools) will be inconsistent and expensive.

## Goals

1. Introduce a native Java tool framework with explicit extension points.
2. Support deterministic discovery, registration, execution, and cache behavior.
3. Ship two concrete built-ins that validate real-world framework usefulness.
4. Keep future Java tool additions low-friction (new class + registration metadata, minimal boilerplate).

## Non-Goals

1. Full plugin marketplace/tool distribution in this step.
2. Non-Java external tool runtime (later roadmap).
3. Comprehensive plugin security sandboxing (belongs to later plugin/security milestones).

## Selected Approach

Implement **foundation + 2 strategic built-ins**:

1. Framework core:
   - native tool interface
   - registry/discovery service
   - execution service
   - deterministic result caching
2. Built-ins:
   - `tool_registry_inspect`
   - `plugin_contract_validate`

This balances immediate value with future extensibility for `v2.6.0`.

## Architecture

### 1. Tool Contract Layer

Define a core interface (or equivalent abstraction) for native Java tools:

- `toolId()` (stable identifier)
- `displayName()`
- `description()`
- `inputSchema()` (JSON-schema-like metadata)
- `isCacheable()`
- `defaultTtlSeconds()` (for cacheable deterministic tools)
- `execute(requestContext, input)` returning structured result

Design rule: adding a new tool must not require editing executor internals.

### 2. Tool Registry

Registry responsibilities:

- collect available native tools at startup
- index by `toolId`
- expose list/detail lookup for runtime and API
- reject duplicate `toolId`

Extensibility requirement:

- registration is declarative (Spring component scan or equivalent)
- no hard-coded switch/case in runtime path

### 3. Tool Execution Service

Execution responsibilities:

- validate tool existence
- validate input against tool schema contract
- execute with standardized context (agent/team/session/correlation)
- normalize success/error response envelope
- emit structured logs for execution start/end/failure

Error model:

- unknown tool -> validation/not-found error
- invalid input -> explicit validation error
- execution failure -> explicit tool execution error with reason code

### 4. Tool Result Caching

Caching applies only when tool is declared cacheable and inputs are deterministic.

Cache key model:

- `toolId + normalizedInput + scopeKey`
- normalization ensures stable key order for maps/JSON

Cache behavior:

- read-through lookup before execution
- store successful deterministic results with TTL
- no cache write for failed executions
- allow per-tool TTL override via config

## Built-in Tools in v2.3.6-dev

### A. `tool_registry_inspect`

Purpose:

- list available tools
- return metadata used by operators/agents:
  - id, description, schema summary, cacheability, ttl hints

Value:

- validates discovery/registration path
- gives direct visibility for debugging and orchestration

### B. `plugin_contract_validate`

Purpose:

- validate Java plugin contract/manifest basics for early plugin flow:
  - required fields presence
  - basic type/format checks
  - compatibility surface checks used by Java-first plugin path

Value:

- immediate utility for v2.6.0 preparation
- validates execution and caching over practical workload

## API Surface (v2.3.6-dev)

Add tool endpoints under existing agent/team operational API area:

1. list tools
2. get tool metadata by id
3. execute tool with input payload

Response envelope should be stable for future plugin-provided tools:

- `toolId`
- `status`
- `result`
- `cached` flag
- `cacheTtlRemaining` (when relevant)
- `error` (when failed)

## Data Model / Persistence

No heavy persistent schema is required for this step.

Primary storage:

- in-memory registry metadata
- Redis-backed cache entries (existing cache infrastructure) or configured cache abstraction

Optional logging records remain in existing system log pipeline.

## Configuration

Add tool framework settings in `application.yml`:

- global enable/disable
- default execution timeout
- default cache TTL
- per-tool TTL overrides
- cache enable toggle

## Testing Strategy

1. Unit tests
   - registry duplicate detection
   - input validation behavior
   - execution success/error envelope
   - cache hit/miss + TTL behavior
2. Integration tests
   - boot-time registration of built-ins
   - API list/execute flow
   - deterministic caching behavior across repeated calls
3. Regression checks
   - existing agent flows unaffected when tool framework is enabled
   - failure paths surface explicit errors

## Roadmap Alignment

Implements roadmap `v2.3.6-dev` items:

- Java-based tool interface
- tool discovery and registration
- tool execution within JVM
- tool result caching

Plus concrete foundation validation via two built-ins:

- `tool_registry_inspect`
- `plugin_contract_validate`

## Risks & Mitigations

1. **Risk:** framework too rigid for future tools  
   **Mitigation:** contract + declarative registration; avoid executor switch logic.

2. **Risk:** incorrect cache reuse  
   **Mitigation:** cache only explicitly cacheable tools with normalized deterministic keys.

3. **Risk:** tool API churn in v2.6.0  
   **Mitigation:** stable execution envelope and metadata schema from first release.

## Success Criteria

1. Two built-in tools run through the same native framework path.
2. New Java tool can be added with minimal boilerplate and no executor rewrites.
3. Cached deterministic tool calls reduce repeated execution cost.
4. API/docs/release artifacts align with `v2.3.6-dev` workflow requirements.
