<script setup lang="ts">
// Implemented in v1.7.7-dev
import { onMounted, ref } from 'vue'
import { fetchLogs, type SystemLog } from '../api'
import { useLiveStore } from '../stores/liveStore'

const live = useLiveStore()
const logs = ref<SystemLog[]>([])

onMounted(async () => {
  try { logs.value = await fetchLogs(50) } catch { /* non-fatal */ }
})
</script>

<template>
  <header><p>Observability</p><h1>Logs</h1></header>
  <section class="panel">
    <h2>
      Live Stream
      <span class="state-badge" :class="live.sseConnected ? 'active' : 'disabled'">
        {{ live.sseConnected ? 'CONNECTED' : 'DISCONNECTED' }}
      </span>
    </h2>
    <ul v-if="live.liveLogs.length" class="log-list">
      <li v-for="entry in live.liveLogs.slice(0, 50)" :key="entry.id">
        <strong>{{ entry.payload?.category ?? entry.type }}</strong>
        <span>{{ entry.payload?.event ?? entry.source }}</span>
        <small>{{ entry.payload?.level ?? '' }} · {{ new Date(entry.occurredAt).toLocaleTimeString() }}</small>
      </li>
    </ul>
    <p v-else>Waiting for live events…</p>
  </section>
  <section class="panel">
    <h2>Recent Logs</h2>
    <ul v-if="logs.length" class="log-list">
      <li v-for="log in logs" :key="log.id">
        <strong>{{ log.category }}</strong>
        <span>{{ log.event }}</span>
        <small>{{ log.level }} · {{ new Date(log.timestamp).toLocaleString() }}</small>
      </li>
    </ul>
    <p v-else>No logs yet.</p>
  </section>
</template>
