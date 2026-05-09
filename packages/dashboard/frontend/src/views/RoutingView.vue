<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchRoutingLogs, type RoutingLog } from '../api'
import { useAppStore } from '../stores/appStore'

const app = useAppStore()
const routingLogs = ref<RoutingLog[]>([])

onMounted(async () => {
  try { routingLogs.value = await fetchRoutingLogs() }
  catch (e) { app.setError(e instanceof Error ? e.message : 'Failed to load routing logs') }
})
</script>

<template>
  <header>
    <p>Orchestration</p>
    <h1>Routing Log</h1>
  </header>

  <section class="panel">
    <h2>Routing Decisions</h2>
    <ul v-if="routingLogs.length" class="log-list">
      <li v-for="entry in routingLogs.slice(0, 100)" :key="entry.id">
        <strong>{{ entry.decision }}</strong>
        <span>{{ entry.targetAgentId ?? entry.targetTeamId ?? entry.targetProjectId ?? '—' }}</span>
        <small>{{ entry.reasoning }} · {{ new Date(entry.createdAt).toLocaleString() }}</small>
      </li>
    </ul>
    <p v-else>No routing decisions recorded yet.</p>
  </section>
</template>
