<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { fetchLogs, type SystemLog } from '../api'
import { useLiveStore } from '../stores/liveStore'

const live = useLiveStore()
const logs = ref<SystemLog[]>([])
const filterLevel = ref('')
const filterCategory = ref('')

onMounted(async () => {
  try { logs.value = await fetchLogs(100) } catch { /* non-fatal */ }
})

const filteredLogs = computed(() => logs.value.filter(l =>
  (!filterLevel.value || l.level === filterLevel.value) &&
  (!filterCategory.value || l.category === filterCategory.value)
))

const filteredLive = computed(() => live.liveLogs.filter(l =>
  (!filterLevel.value || l.payload?.level === filterLevel.value) &&
  (!filterCategory.value || l.payload?.category === filterCategory.value)
))

const categories = computed(() => [...new Set(logs.value.map(l => l.category))].sort())
const levels = ['DEBUG', 'INFO', 'WARN', 'ERROR']

function levelClass(level: string) {
  return level === 'ERROR' ? 'disabled' : level === 'WARN' ? 'paused' : 'active'
}
</script>

<template>
  <header>
    <p>Observability</p>
    <h1>Logs</h1>
  </header>

  <div class="log-filters">
    <select v-model="filterLevel">
      <option value="">All Levels</option>
      <option v-for="l in levels" :key="l" :value="l">{{ l }}</option>
    </select>
    <select v-model="filterCategory">
      <option value="">All Categories</option>
      <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
    </select>
  </div>

  <section class="panel">
    <h2>
      Live Stream
      <span class="state-badge" :class="live.sseConnected ? 'active' : 'disabled'">
        {{ live.sseConnected ? 'CONNECTED' : 'DISCONNECTED' }}
      </span>
      <small style="color:#aab4bf;font-size:0.8rem;margin-left:8px">{{ filteredLive.length }} events</small>
    </h2>
    <ul v-if="filteredLive.length" class="log-list">
      <li v-for="entry in filteredLive.slice(0, 50)" :key="entry.id">
        <strong>{{ entry.payload?.category ?? entry.type }}</strong>
        <span>{{ entry.payload?.event ?? entry.source }}</span>
        <small>
          <span class="state-badge" :class="levelClass(entry.payload?.level ?? '')">{{ entry.payload?.level ?? '' }}</span>
          · {{ new Date(entry.occurredAt).toLocaleTimeString() }}
        </small>
      </li>
    </ul>
    <p v-else>Waiting for live events…</p>
  </section>

  <section class="panel">
    <h2>
      Recent Logs (historic)
      <small style="color:#aab4bf;font-size:0.8rem;margin-left:8px">{{ filteredLogs.length }} entries</small>
    </h2>
    <ul v-if="filteredLogs.length" class="log-list">
      <li v-for="log in filteredLogs" :key="log.id">
        <strong>{{ log.category }}</strong>
        <span>{{ log.event }}</span>
        <small>
          <span class="state-badge" :class="levelClass(log.level)">{{ log.level }}</span>
          · {{ new Date(log.timestamp).toLocaleString() }}
        </small>
      </li>
    </ul>
    <p v-else>No logs match current filters.</p>
  </section>
</template>
