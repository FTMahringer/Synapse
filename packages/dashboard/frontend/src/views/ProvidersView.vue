<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAppStore } from '../stores/appStore'

interface ModelProvider {
  id: string
  name: string
  type: string
  enabled: boolean
  config: Record<string, unknown>
  createdAt: string
}

const app = useAppStore()
const providers = ref<ModelProvider[]>([])
const showForm = ref(false)
const form = ref({ name: '', type: 'OLLAMA', baseUrl: '', apiKey: '' })

const API_BASE = import.meta.env.VITE_API_BASE ?? ''

function authHeader() {
  const t = localStorage.getItem('synapse_token')
  return t ? { Authorization: `Bearer ${t}` } : {}
}

onMounted(load)

async function load() {
  try {
    const res = await fetch(`${API_BASE}/api/providers`, { headers: authHeader() })
    if (res.ok) providers.value = await res.json()
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Failed to load providers') }
}

async function createProvider() {
  try {
    const body = {
      name: form.value.name,
      type: form.value.type,
      config: {
        baseUrl: form.value.baseUrl,
        ...(form.value.apiKey ? { apiKey: form.value.apiKey } : {}),
      },
    }
    const res = await fetch(`${API_BASE}/api/providers`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify(body),
    })
    if (!res.ok) throw new Error(`Create failed: ${res.status}`)
    showForm.value = false
    form.value = { name: '', type: 'OLLAMA', baseUrl: '', apiKey: '' }
    await load()
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Create provider failed') }
}

async function deleteProvider(id: string) {
  try {
    await fetch(`${API_BASE}/api/providers/${id}`, { method: 'DELETE', headers: authHeader() })
    await load()
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Delete failed') }
}

async function testProvider(id: string) {
  try {
    const res = await fetch(`${API_BASE}/api/providers/${id}/test`, {
      method: 'POST',
      headers: authHeader(),
    })
    const data = await res.json()
    alert(data.success ? `✓ ${data.message ?? 'Provider reachable'}` : `✗ ${data.message ?? 'Test failed'}`)
  } catch { alert('Test request failed') }
}
</script>

<template>
  <header>
    <p>Model Providers</p>
    <h1>Providers</h1>
  </header>

  <p v-if="app.error" class="error">{{ app.error }}</p>

  <section class="panel">
    <div class="panel-header">
      <h2>Configured Providers</h2>
      <button @click="showForm = !showForm">+ Add Provider</button>
    </div>

    <form v-if="showForm" class="inline-form" @submit.prevent="createProvider">
      <input v-model="form.name" placeholder="Name (e.g. Local Ollama)" required />
      <select v-model="form.type">
        <option value="OLLAMA">Ollama</option>
        <option value="OPENAI">OpenAI-compatible</option>
        <option value="ANTHROPIC">Anthropic</option>
      </select>
      <input v-model="form.baseUrl" placeholder="Base URL (e.g. http://ollama:11434)" />
      <input v-model="form.apiKey" placeholder="API Key (optional)" type="password" />
      <button type="submit">Create</button>
    </form>

    <ul v-if="providers.length" class="agent-list" style="margin-top:12px">
      <li v-for="p in providers" :key="p.id">
        <div class="agent-row">
          <div>
            <span>{{ p.name }}</span>
            <small>{{ p.type }} · {{ p.id }}</small>
          </div>
          <div class="agent-controls">
            <span class="state-badge" :class="p.enabled ? 'active' : 'disabled'">
              {{ p.enabled ? 'ENABLED' : 'DISABLED' }}
            </span>
            <button @click="testProvider(p.id)">Test</button>
            <button @click="deleteProvider(p.id)">Delete</button>
          </div>
        </div>
      </li>
    </ul>
    <p v-else style="margin-top:12px">No providers configured.</p>
  </section>
</template>
