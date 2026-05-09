<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchPlugins, enablePlugin, disablePlugin, uninstallPlugin, type Plugin } from '../api'
import { useAppStore } from '../stores/appStore'

const app = useAppStore()
const plugins = ref<Plugin[]>([])
const showInstallForm = ref(false)
const installConfirmed = ref(false)
const manifest = ref({ id: '', name: '', type: 'SKILL', version: '1.0.0', author: '', source: '' })

const API_BASE = import.meta.env.VITE_API_BASE ?? ''
function authH() { const t = localStorage.getItem('synapse_token'); return t ? { Authorization: `Bearer ${t}` } : {} }

onMounted(load)

async function load() {
  try { plugins.value = await fetchPlugins() }
  catch (e) { app.setError(e instanceof Error ? e.message : 'Failed to load plugins') }
}

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

async function installPlugin() {
  try {
    const url = `${API_BASE}/api/plugins/install${installConfirmed.value ? '?confirmed=true' : ''}`
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authH() },
      body: JSON.stringify(manifest.value),
    })
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error(err.message ?? `Install failed: ${res.status}`)
    }
    showInstallForm.value = false
    installConfirmed.value = false
    manifest.value = { id: '', name: '', type: 'SKILL', version: '1.0.0', author: '', source: '' }
    await load()
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Install failed') }
}
</script>

<template>
  <header><p>Plugin Runtime</p><h1>Plugins</h1></header>
  <p v-if="app.error" class="error">{{ app.error }}</p>

  <section class="panel">
    <div class="panel-header">
      <h2>Installed Plugins</h2>
      <button @click="showInstallForm = !showInstallForm">+ Install Plugin</button>
    </div>

    <form v-if="showInstallForm" class="inline-form" @submit.prevent="installPlugin">
      <input v-model="manifest.id" placeholder="Plugin ID (e.g. my-skill)" required />
      <input v-model="manifest.name" placeholder="Name" required />
      <select v-model="manifest.type">
        <option value="SKILL">Skill</option>
        <option value="CHANNEL">Channel</option>
        <option value="MODEL">Model</option>
        <option value="MCP">MCP</option>
      </select>
      <input v-model="manifest.version" placeholder="Version (e.g. 1.0.0)" required />
      <input v-model="manifest.author" placeholder="Author" required />
      <input v-model="manifest.source" placeholder="Source (e.g. official, community)" />
      <label style="display:flex;gap:8px;align-items:center;color:#aab4bf;font-size:0.85rem">
        <input type="checkbox" v-model="installConfirmed" />
        I confirm installing this community/unverified plugin
      </label>
      <button type="submit">Install</button>
    </form>

    <ul v-if="plugins.length" class="agent-list" style="margin-top:12px">
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
    <p v-else style="margin-top:12px">No plugins installed.</p>
  </section>
</template>
