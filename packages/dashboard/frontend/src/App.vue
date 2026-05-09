<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import {
  fetchAgents,
  fetchAgentRuntimes,
  fetchHealth,
  fetchLogs,
  fetchRoutingLogs,
  connectLogStream,
  activateAgent,
  pauseAgent,
  disableAgent,
  type AgentDefinition,
  type AgentRuntime,
  type HealthResponse,
  type LiveLogEvent,
  type RoutingLog,
  type SystemLog,
} from './api'

const health = ref<HealthResponse | null>(null)
const agents = ref<AgentDefinition[]>([])
const runtimes = ref<AgentRuntime[]>([])
const logs = ref<SystemLog[]>([])
const liveLogs = ref<LiveLogEvent[]>([])
const routingLogs = ref<RoutingLog[]>([])
const error = ref<string | null>(null)
const activeTab = ref<'overview' | 'agents' | 'routing' | 'logs'>('overview')
const liveConnected = ref(false)

let logStream: EventSource | null = null

onMounted(async () => {
  await reload()
  logStream = connectLogStream(
    (event) => {
      liveLogs.value.unshift(event)
      if (liveLogs.value.length > 100) liveLogs.value.pop()
      liveConnected.value = true
    },
    () => { liveConnected.value = false }
  )
})

onUnmounted(() => {
  logStream?.close()
})

async function reload() {
  try {
    const [healthRes, agentsRes, runtimesRes, logsRes, routingRes] = await Promise.all([
      fetchHealth(),
      fetchAgents(),
      fetchAgentRuntimes(),
      fetchLogs(),
      fetchRoutingLogs(),
    ])
    health.value = healthRes
    agents.value = agentsRes
    runtimes.value = runtimesRes
    logs.value = logsRes
    routingLogs.value = routingRes
    error.value = null
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to reach backend'
  }
}

function getRuntimeState(agentId: string): AgentRuntime | undefined {
  return runtimes.value.find(r => r.agentId === agentId)
}

async function setAgentState(agentId: string, action: 'activate' | 'pause' | 'disable') {
  try {
    const fn = action === 'activate' ? activateAgent : action === 'pause' ? pauseAgent : disableAgent
    const updated = await fn(agentId)
    const idx = runtimes.value.findIndex(r => r.agentId === agentId)
    if (idx >= 0) runtimes.value[idx] = updated
    else runtimes.value.push(updated)
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Action failed'
  }
}
</script>

<template>
  <main class="shell">
    <aside class="sidebar">
      <div class="brand">SYNAPSE</div>
      <nav>
        <a :class="{ active: activeTab === 'overview' }" @click="activeTab = 'overview'">Overview</a>
        <a :class="{ active: activeTab === 'agents' }" @click="activeTab = 'agents'">Agents</a>
        <a :class="{ active: activeTab === 'routing' }" @click="activeTab = 'routing'">Routing</a>
        <a :class="{ active: activeTab === 'logs' }" @click="activeTab = 'logs'">Logs</a>
      </nav>
    </aside>

    <section class="content">
      <header>
        <p>Runtime</p>
        <h1>Operator Console</h1>
      </header>

      <p v-if="error" class="error">{{ error }}</p>

      <!-- Overview Tab -->
      <template v-if="activeTab === 'overview'">
        <section class="status-grid">
          <article>
            <span>Backend</span>
            <strong>{{ health?.status ?? 'Checking' }}</strong>
          </article>
          <article>
            <span>Version</span>
            <strong>{{ health?.version ?? 'v1.0.0' }}</strong>
          </article>
          <article>
            <span>ECHO</span>
            <strong>{{ health?.echoActivation ?? 'manual' }}</strong>
          </article>
          <article>
            <span>Agents</span>
            <strong>{{ agents.length }}</strong>
          </article>
        </section>

        <section class="panel">
          <h2>{{ health?.systemName ?? 'SYNAPSE' }}</h2>
          <p v-if="health">
            Backend responded at {{ new Date(health.timestamp).toLocaleString() }}.
            ECHO debug-only mode is {{ health.echoDebugOnly ? 'enabled' : 'disabled' }}.
          </p>
          <p v-else>Waiting for backend health response.</p>
        </section>

        <section class="panel">
          <h2>Recent Logs</h2>
          <ul v-if="logs.length" class="log-list">
            <li v-for="log in logs.slice(0, 10)" :key="log.id">
              <strong>{{ log.category }}</strong>
              <span>{{ log.event }}</span>
              <small>{{ log.level }} · {{ new Date(log.timestamp).toLocaleTimeString() }}</small>
            </li>
          </ul>
          <p v-else>No logs available yet.</p>
        </section>
      </template>

      <!-- Agents Tab -->
      <template v-if="activeTab === 'agents'">
        <section class="panel">
          <h2>Agent Registry</h2>
          <ul v-if="agents.length" class="agent-list">
            <li v-for="agent in agents" :key="agent.id">
              <div class="agent-row">
                <div>
                  <span>{{ agent.name }}</span>
                  <small>{{ agent.type }} · {{ agent.id }}</small>
                </div>
                <div class="agent-controls">
                  <span
                    class="state-badge"
                    :class="getRuntimeState(agent.id)?.state?.toLowerCase() ?? 'unknown'"
                  >
                    {{ getRuntimeState(agent.id)?.state ?? 'UNKNOWN' }}
                  </span>
                  <button @click="setAgentState(agent.id, 'activate')">Activate</button>
                  <button @click="setAgentState(agent.id, 'pause')">Pause</button>
                  <button @click="setAgentState(agent.id, 'disable')">Disable</button>
                </div>
              </div>
            </li>
          </ul>
          <p v-else>No agents loaded yet.</p>
        </section>

        <section class="panel">
          <h2>Runtime States</h2>
          <ul v-if="runtimes.length" class="log-list">
            <li v-for="rt in runtimes" :key="rt.agentId">
              <strong>{{ rt.agentId }}</strong>
              <span class="state-badge" :class="rt.state.toLowerCase()">{{ rt.state }}</span>
              <small>
                Last active: {{ rt.lastActivatedAt ? new Date(rt.lastActivatedAt).toLocaleString() : 'never' }}
              </small>
            </li>
          </ul>
          <p v-else>No runtime records yet.</p>
        </section>
      </template>

      <!-- Routing Tab -->
      <template v-if="activeTab === 'routing'">
        <section class="panel">
          <h2>Routing Log</h2>
          <ul v-if="routingLogs.length" class="log-list">
            <li v-for="entry in routingLogs.slice(0, 50)" :key="entry.id">
              <strong>{{ entry.decision }}</strong>
              <span>{{ entry.targetAgentId ?? entry.targetTeamId ?? entry.targetProjectId ?? '—' }}</span>
              <small>{{ entry.reasoning }} · {{ new Date(entry.createdAt).toLocaleString() }}</small>
            </li>
          </ul>
          <p v-else>No routing decisions recorded yet.</p>
        </section>
      </template>

      <!-- Logs Tab -->
      <template v-if="activeTab === 'logs'">
        <section class="panel">
          <h2>
            Live Stream
            <span class="state-badge" :class="liveConnected ? 'active' : 'disabled'">
              {{ liveConnected ? 'CONNECTED' : 'DISCONNECTED' }}
            </span>
          </h2>
          <ul v-if="liveLogs.length" class="log-list">
            <li v-for="entry in liveLogs" :key="entry.id">
              <strong>{{ entry.payload?.category ?? entry.type }}</strong>
              <span>{{ entry.payload?.event ?? entry.source }}</span>
              <small>{{ entry.payload?.level ?? '' }} · {{ new Date(entry.occurredAt).toLocaleTimeString() }}</small>
            </li>
          </ul>
          <p v-else>Waiting for live log events…</p>
        </section>

        <section class="panel">
          <h2>Recent Logs (historic)</h2>
          <ul v-if="logs.length" class="log-list">
            <li v-for="log in logs" :key="log.id">
              <strong>{{ log.category }}</strong>
              <span>{{ log.event }}</span>
              <small>{{ log.level }} · {{ new Date(log.timestamp).toLocaleString() }}</small>
            </li>
          </ul>
          <p v-else>No logs available yet.</p>
        </section>
      </template>
    </section>
  </main>
</template>
