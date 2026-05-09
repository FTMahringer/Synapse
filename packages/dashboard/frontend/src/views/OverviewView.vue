<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchHealth, fetchAgents, type HealthResponse, type AgentDefinition } from '../api'
import { useLiveStore } from '../stores/liveStore'
import { useAppStore } from '../stores/appStore'

const app = useAppStore()
const live = useLiveStore()

const health = ref<HealthResponse | null>(null)
const agents = ref<AgentDefinition[]>([])

onMounted(async () => {
  try {
    const [h, a] = await Promise.all([fetchHealth(), fetchAgents()])
    health.value = h
    agents.value = a
  } catch (e) {
    app.setError(e instanceof Error ? e.message : 'Failed to load overview')
  }
})
</script>

<template>
  <header>
    <p>Runtime</p>
    <h1>Operator Console</h1>
  </header>

  <p v-if="app.error" class="error">{{ app.error }}</p>

  <section class="status-grid">
    <article>
      <span>Backend</span>
      <strong>{{ health?.status ?? '…' }}</strong>
    </article>
    <article>
      <span>Version</span>
      <strong>{{ health?.version ?? '…' }}</strong>
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
    <h2>
      Conversation Stream
      <span class="state-badge" :class="live.wsConnected ? 'active' : 'disabled'">
        {{ live.wsConnected ? 'LIVE' : 'OFFLINE' }}
      </span>
    </h2>
    <ul v-if="live.conversationEvents.length" class="log-list">
      <li v-for="event in live.conversationEvents.slice(0, 20)" :key="event.id">
        <strong>{{ event.type }}</strong>
        <span>{{ event.source }}</span>
        <small>{{ new Date(event.occurredAt).toLocaleTimeString() }}</small>
      </li>
    </ul>
    <p v-else>No conversation events yet.</p>
  </section>
</template>
