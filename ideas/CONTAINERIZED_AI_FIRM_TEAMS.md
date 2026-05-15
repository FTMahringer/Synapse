# Containerized AI-Firm & Agent Teams Architecture

Detailed design for containerized multi-agent architecture with AI-Firm and Teams.

---

## Container Architecture Models

### Model A: Hierarchical (Recommended)

```
[Main Agent Container] ←→ [AI-Firm Container] ←→ [Team Lead Containers] ←→ [Team Agent Containers]
```

- Main Agent is the entry point (always running)
- AI-Firm is separate container (if active)
- Each Team has a Lead container + Agent containers
- Communication via message bus with clear hierarchy

**Pros**: Clear ownership, hierarchical delegation, fault isolation per level
**Cons**: More hops, more complexity, latency at each level

### Model B: Flat Peer-to-Peer

```
[Main Agent] ←→ [AI-Firm] ←→ [Team A] ←→ [Team B]
    ↓            ↓           ↓            ↓
[Container]  [Container] [Container]  [Container]
```

- All agents are peers
- AI-Firm has "orchestrator" role but is peer
- Teams are logical groupings, not structural
- Communication via message bus (any-to-any)

**Pros**: Simpler, fewer hops, more resilient
**Cons**: Less clear ownership, harder to debug, no natural hierarchy

### Model C: Hybrid (Main + AI-Firm Co-located)

```
[Main + AI-Firm Container] ←→ [Team A Container Group]
                              ←→ [Team B Container Group]
```

- Main Agent and AI-Firm share container (tightly coupled)
- Each Team is a container group (Lead + Agents in same container/namespace)
- Communication: Main+AI-Firm ↔ Team Groups

**Pros**: Fewer hops for Main↔AI-Firm, team isolation, simpler team logic
**Cons**: Team Lead is single point of failure per team

---

## AI-Firm Container Design

### Core Responsibilities
- Project orchestration across teams
- Goal tracking and deadline management
- Cross-team task delegation
- Project state management

### Container Requirements
- **State**: Redis-backed (survives container restarts)
- **Communication**: Pub/Sub with all teams
- **Scaling**: Active-passive failover (only one AI-Firm active)
- **Startup**: Waits for Main Agent to be healthy

### Failure Modes
- **AI-Firm crash**: Main Agent detects via heartbeat, promotes standby or pauses projects
- **State loss**: State in Redis, always recoverable
- **Communication loss**: Queue messages, retry on reconnect

---

## Team Architecture

### Team Container Group

```
[Team Lead Container] ←→ [Specialist 1] ←→ [Specialist 2] ←→ ...
     (Coordinator)        (e.g., code)      (e.g., docs)
```

- **Team Lead**: Orchestrates work within team, delegates to specialists, aggregates results
- **Specialists**: Execute specific tasks (code, research, writing, etc.)
- **Shared**: Team memory, team artifacts, team config

### Team Communication
- Team Lead ↔ Specialists: Direct message bus or shared in-memory
- Team Lead → Main/AI-Firm: Report completion, request clarification
- Specialists → Main/AI-Firm: Return results, request input

### Team Failure Modes
- **Lead crash**: Specialists idle, Main/AI-Firm promotes new Lead or redistributes
- **Specialist crash**: Lead detects, retries task or routes to different specialist
- **Full team failure**: Main/AI-Firm recreates team from template

---

## Inter-Container Communication

### Message Bus Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Redis/RabbitMQ                    │
│                    (Message Bus)                    │
├─────────────────────────────────────────────────────┤
│  Channel: main→aifirm    Channel: aifirm→teams      │
│  Channel: teams→aifirm  Channel: main→team-N       │
│  Channel: team-N→main   Channel: team-lead→agents  │
│  ...                                                │
└─────────────────────────────────────────────────────┘
```

### Message Types
- **Request**: Task delegation with payload, expected response
- **Response**: Task completion, results, errors
- **Event**: State changes, heartbeats, notifications
- **Broadcast**: Team-wide announcements, system alerts

### Protocol
- JSON envelopes with: `type`, `sender`, `recipient`, `payload`, `correlationId`, `timestamp`
- Request-Response via correlation IDs
- Acknowledgment for critical messages

---

## Shared Storage Design

### Global Layer (All Agents)
```
/shared/
├── skills/          # All available skills
├── plugins/          # All available plugins
├── models/           # Model configs, embeddings cache
└── artifacts/       # Shared artifacts (team-agnostic)
```

### Team Layer (Per Team)
```
/shared/teams/team-code/
├── memory/          # Team knowledge base
├── artifacts/       # Code files, PRs, reviews
└── state/           # Team state (tasks, priorities)
```

### Agent Layer (Per Agent)
```
/shared/agents/agent-code-1/
├── memory/          # Agent personal memory
├── config/          # Agent-specific config
└── cache/           # Temp cache (can be cleared)
```

### Storage Access
- Global: Read-only for skills/plugins, read-write for artifacts
- Team: Read-write by team members, read for others
- Agent: Read-write only for self

---

## State Management

### Distributed State Stores

| State Type | Storage | Access Pattern |
|------------|---------|----------------|
| Project/Goals | Redis | AI-Firm writes, Main reads |
| Team Tasks | Redis + Local | Team Lead writes, Specialists read |
| Agent Memory | Local + Sync | Agent writes, periodic sync |
| Session | Redis | Shared access |

### State Synchronization
- **Redis**: Primary state store (all containers can access)
- **Local Cache**: Performance optimization (read-through cache)
- **Sync Protocol**: Periodic sync to Redis for durability

### Consistency Model
- Eventual consistency for agent memory
- Strong consistency for coordination state (Redis transactions)
- Optimistic locking for concurrent access

---

## Resource Allocation

### Per-Agent Limits
- CPU: 0.5 - 2 cores (configurable)
- RAM: 1-4 GB (configurable)
- GPU: Optional, via device plugins
- Ephemeral storage: 5-20 GB

### Team Limits
- Total team: Sum of agent limits × 1.2 (overhead)
- Burst: Allow 10% burst for spikes

### Priority Levels
- **Critical** (Main Agent): Always schedule
- **High** (AI-Firm): High priority
- **Normal** (Team Leads): Default
- **Low** (Specialists): Background tasks

---

## Scaling Strategies

### Horizontal Scaling
- Add more agent containers for load
- Teams can have multiple specialists of same type (code agents → pool)
- Load balancer routes to least busy agent

### Vertical Scaling
- Increase resources for heavy agents (LLM-heavy tasks)
- Team Lead gets more resources than specialists

### Pre-warming
- Keep "warm" agent containers ready
- On-demand scaling from warm pool
- Cost vs. latency trade-off

---

## Implementation Phases

### Phase 1: Core Infrastructure
1. Message bus setup (Redis pub/sub)
2. Agent container template
3. Agent registry (who is running where)
4. Basic health monitoring

### Phase 2: Single-Agent Containers
1. Main Agent containerizes
2. Agent can access shared skills/plugins
3. Basic communication works

### Phase 3: Multi-Agent
1. Agent Teams containerized
2. Team Lead + Specialists in containers
3. Team communication via bus

### Phase 4: AI-Firm Container
1. AI-Firm containerized
2. Project orchestration across team containers
3. State management in Redis

### Phase 5: Production Hardening
1. Failover and HA
2. Resource quotas enforced
3. Monitoring and alerting
4. Auto-scaling policies