<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchAgents, fetchAgentRuntimes, activateAgent, pauseAgent, disableAgent, type AgentDefinition, type AgentRuntime } from '../api'
import { useAppStore } from '../stores/appStore'

const app = useAppStore()
const agents = ref<AgentDefinition[]>([])
const runtimes = ref<AgentRuntime[]>([])

onMounted(async () => {
  try {
    const [a, r] = await Promise.all([fetchAgents(), fetchAgentRuntimes()])
    agents.value = a
    runtimes.value = r
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Failed to load agents') }
})

function getRuntime(id: string) {
  return runtimes.value.find(r => r.agentId === id)
}

async function setState(id: string, action: 'activate' | 'pause' | 'disable') {
  try {
    const fn = action === 'activate' ? activateAgent : action === 'pause' ? pauseAgent : disableAgent
    const updated = await fn(id)
    const idx = runtimes.value.findIndex(r => r.agentId === id)
    if (idx >= 0) runtimes.value[idx] = updated
    else runtimes.value.push(updated)
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Action failed') }
}
</script>

<template>
  <header>
    <p>Orchestration</p>
    <h1>Agents</h1>
  </header>

  <p v-if="app.error" class="error">{{ app.error }}</p>

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
            <span class="state-badge" :class="getRuntime(agent.id)?.state?.toLowerCase() ?? 'unknown'">
              {{ getRuntime(agent.id)?.state ?? 'UNKNOWN' }}
            </span>
            <button @click="setState(agent.id, 'activate')">Activate</button>
            <button @click="setState(agent.id, 'pause')">Pause</button>
            <button @click="setState(agent.id, 'disable')">Disable</button>
          </div>
        </div>
      </li>
    </ul>
    <p v-else>No agents loaded.</p>
  </section>

  <section class="panel">
    <h2>Runtime States</h2>
    <ul v-if="runtimes.length" class="log-list">
      <li v-for="rt in runtimes" :key="rt.agentId">
        <strong>{{ rt.agentId }}</strong>
        <span class="state-badge" :class="rt.state.toLowerCase()">{{ rt.state }}</span>
        <small>Last active: {{ rt.lastActivatedAt ? new Date(rt.lastActivatedAt).toLocaleString() : 'never' }}</small>
      </li>
    </ul>
    <p v-else>No runtime records.</p>
  </section>
</template>
