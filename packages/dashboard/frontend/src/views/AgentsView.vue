<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchAgents, fetchAgentRuntimes, activateAgent, pauseAgent, disableAgent, type AgentDefinition, type AgentRuntime } from '../api'
import { useAppStore } from '../stores/appStore'

interface Team {
  id: string
  name: string
  leaderAgentId: string
  createdAt: string
}

const app = useAppStore()
const agents = ref<AgentDefinition[]>([])
const runtimes = ref<AgentRuntime[]>([])
const teams = ref<Team[]>([])
const activePanel = ref<'agents' | 'teams'>('agents')
const showTeamForm = ref(false)
const teamForm = ref({ id: '', name: '', leaderAgentId: '' })

const API_BASE = import.meta.env.VITE_API_BASE ?? ''
function authH(): Record<string, string> { const t = localStorage.getItem("synapse_token"); return t ? { Authorization: `Bearer ${t}` } : {} }

onMounted(async () => {
  try {
    const [a, r, tm] = await Promise.all([
      fetchAgents(),
      fetchAgentRuntimes(),
      fetch(`${API_BASE}/api/teams`, { headers: authH() }).then(r => r.json()),
    ])
    agents.value = a
    runtimes.value = r
    teams.value = tm
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Failed to load') }
})

function getRuntime(id: string) { return runtimes.value.find(r => r.agentId === id) }

async function setState(id: string, action: 'activate' | 'pause' | 'disable') {
  try {
    const fn = action === 'activate' ? activateAgent : action === 'pause' ? pauseAgent : disableAgent
    const updated = await fn(id)
    const idx = runtimes.value.findIndex(r => r.agentId === id)
    if (idx >= 0) runtimes.value[idx] = updated
    else runtimes.value.push(updated)
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Action failed') }
}

async function createTeam() {
  try {
    const res = await fetch(`${API_BASE}/api/teams`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authH() },
      body: JSON.stringify(teamForm.value),
    })
    if (!res.ok) throw new Error(`Create failed: ${res.status}`)
    teams.value.push(await res.json())
    showTeamForm.value = false
    teamForm.value = { id: '', name: '', leaderAgentId: '' }
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Create team failed') }
}

async function deleteTeam(id: string) {
  try {
    await fetch(`${API_BASE}/api/teams/${id}`, { method: 'DELETE', headers: authH() })
    teams.value = teams.value.filter(t => t.id !== id)
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Delete failed') }
}
</script>

<template>
  <header>
    <p>Orchestration</p>
    <h1>Agents &amp; Teams</h1>
  </header>

  <p v-if="app.error" class="error">{{ app.error }}</p>

  <div class="tab-bar">
    <button :class="{ active: activePanel === 'agents' }" @click="activePanel = 'agents'">Agents</button>
    <button :class="{ active: activePanel === 'teams' }" @click="activePanel = 'teams'">Teams</button>
  </div>

  <!-- Agents panel -->
  <template v-if="activePanel === 'agents'">
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

  <!-- Teams panel -->
  <template v-if="activePanel === 'teams'">
    <section class="panel">
      <div class="panel-header">
        <h2>Teams</h2>
        <button @click="showTeamForm = !showTeamForm">+ New Team</button>
      </div>

      <form v-if="showTeamForm" class="inline-form" @submit.prevent="createTeam">
        <input v-model="teamForm.id" placeholder="Team ID (e.g. dev-team)" required />
        <input v-model="teamForm.name" placeholder="Team Name" required />
        <input v-model="teamForm.leaderAgentId" placeholder="Leader Agent ID" required />
        <button type="submit">Create</button>
      </form>

      <ul v-if="teams.length" class="agent-list" style="margin-top:12px">
        <li v-for="team in teams" :key="team.id">
          <div class="agent-row">
            <div>
              <span>{{ team.name }}</span>
              <small>Leader: {{ team.leaderAgentId }} · {{ team.id }}</small>
            </div>
            <div class="agent-controls">
              <button @click="deleteTeam(team.id)">Delete</button>
            </div>
          </div>
        </li>
      </ul>
      <p v-else style="margin-top:12px">No teams configured.</p>
    </section>
  </template>
</template>
