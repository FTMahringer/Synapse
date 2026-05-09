<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchStore, installBundle, type StoreEntry } from '../api'
import { useAppStore } from '../stores/appStore'

const app = useAppStore()
const entries = ref<StoreEntry[]>([])

onMounted(async () => {
  try { entries.value = await fetchStore() }
  catch (e) { app.setError(e instanceof Error ? e.message : 'Failed to load store') }
})

async function doInstallBundle(id: string) {
  try {
    const result = await installBundle(id)
    if (!result.success) app.setError('Bundle install failed: ' + result.errors.join(', '))
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Bundle install failed') }
}
</script>

<template>
  <header><p>Plugin Runtime</p><h1>Store</h1></header>
  <p v-if="app.error" class="error">{{ app.error }}</p>
  <section class="panel">
    <h2>Registry</h2>
    <ul v-if="entries.length" class="agent-list">
      <li v-for="entry in entries" :key="entry.id">
        <div class="agent-row">
          <div>
            <span>{{ entry.name }}</span>
            <small>{{ entry.type }} · {{ entry.source }} · v{{ entry.version }}</small>
            <small v-if="entry.description">{{ entry.description }}</small>
          </div>
          <div class="agent-controls">
            <button v-if="entry.type === 'BUNDLE'" @click="doInstallBundle(entry.id)">Install Bundle</button>
            <span v-else class="state-badge unknown">{{ entry.type }}</span>
          </div>
        </div>
      </li>
    </ul>
    <p v-else>No store entries loaded.</p>
  </section>
</template>
