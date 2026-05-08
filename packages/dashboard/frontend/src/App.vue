<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchAgents, fetchHealth, fetchLogs, type AgentDefinition, type HealthResponse, type SystemLog } from './api'

const health = ref<HealthResponse | null>(null)
const agents = ref<AgentDefinition[]>([])
const logs = ref<SystemLog[]>([])
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    const [healthResponse, agentResponse, logResponse] = await Promise.all([fetchHealth(), fetchAgents(), fetchLogs()])
    health.value = healthResponse
    agents.value = agentResponse
    logs.value = logResponse
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to reach backend'
  }
})
</script>

<template>
  <main class="shell">
    <aside class="sidebar">
      <div class="brand">SYNAPSE</div>
      <nav>
        <a class="active">Overview</a>
        <a>Agents</a>
        <a>Logs</a>
        <a>Store</a>
        <a>Settings</a>
      </nav>
    </aside>

    <section class="content">
      <header>
        <p>Runtime</p>
        <h1>Operator Console</h1>
      </header>

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
        <p v-else-if="error" class="error">{{ error }}</p>
        <p v-else>Waiting for backend health response.</p>
      </section>

      <section class="panel">
        <h2>Agents</h2>
        <ul v-if="agents.length" class="agent-list">
          <li v-for="agent in agents" :key="agent.id">
            <span>{{ agent.name }}</span>
            <small>{{ agent.type }} · {{ agent.path }}</small>
          </li>
        </ul>
        <p v-else>No agents loaded yet.</p>
      </section>

      <section class="panel">
        <h2>Recent Logs</h2>
        <ul v-if="logs.length" class="log-list">
          <li v-for="log in logs" :key="log.id">
            <strong>{{ log.category }}</strong>
            <span>{{ log.event }}</span>
            <small>{{ log.level }} · {{ new Date(log.timestamp).toLocaleTimeString() }}</small>
          </li>
        </ul>
        <p v-else>No logs available yet.</p>
      </section>
    </section>
  </main>
</template>
