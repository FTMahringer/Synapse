<script setup lang="ts">
// Implemented in v1.7.6-dev
import { onMounted, ref } from 'vue'
import { fetchPlugins, enablePlugin, disablePlugin, uninstallPlugin, type Plugin } from '../api'
import { useAppStore } from '../stores/appStore'

const app = useAppStore()
const plugins = ref<Plugin[]>([])

onMounted(async () => {
  try { plugins.value = await fetchPlugins() }
  catch (e) { app.setError(e instanceof Error ? e.message : 'Failed to load plugins') }
})

async function setPluginState(id: string, action: 'enable' | 'disable' | 'uninstall') {
  try {
    if (action === 'uninstall') {
      await uninstallPlugin(id)
      plugins.value = plugins.value.filter(p => p.id !== id)
    } else {
      const updated = action === 'enable' ? await enablePlugin(id) : await disablePlugin(id)
      const idx = plugins.value.findIndex(p => p.id === id)
      if (idx >= 0) plugins.value[idx] = updated
    }
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Plugin action failed') }
}
</script>

<template>
  <header><p>Plugin Runtime</p><h1>Plugins</h1></header>
  <p v-if="app.error" class="error">{{ app.error }}</p>
  <section class="panel">
    <h2>Installed Plugins</h2>
    <ul v-if="plugins.length" class="agent-list">
      <li v-for="plugin in plugins" :key="plugin.id">
        <div class="agent-row">
          <div>
            <span>{{ plugin.name }}</span>
            <small>{{ plugin.type }} · v{{ plugin.version }}</small>
          </div>
          <div class="agent-controls">
            <span class="state-badge" :class="plugin.status === 'INSTALLED' ? 'active' : 'disabled'">{{ plugin.status }}</span>
            <button @click="setPluginState(plugin.id, 'enable')">Enable</button>
            <button @click="setPluginState(plugin.id, 'disable')">Disable</button>
            <button @click="setPluginState(plugin.id, 'uninstall')">Uninstall</button>
          </div>
        </div>
      </li>
    </ul>
    <p v-else>No plugins installed.</p>
  </section>
</template>
