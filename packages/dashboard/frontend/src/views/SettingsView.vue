<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAppStore } from '../stores/appStore'
import { useAuthStore } from '../stores/authStore'

interface SysUser {
  id: string
  username: string
  email: string
  role: string
  createdAt: string
}

interface SystemSetting {
  key: string
  value: unknown
  category: string
}

const app = useAppStore()
const auth = useAuthStore()
const users = ref<SysUser[]>([])
const settings = ref<SystemSetting[]>([])
const activePanel = ref<'users' | 'settings'>('users')
const showUserForm = ref(false)
const userForm = ref({ username: '', email: '', role: 'USER', password: '' })

const API_BASE = import.meta.env.VITE_API_BASE ?? ''
function authH(): Record<string, string> { const t = localStorage.getItem("synapse_token"); return t ? { Authorization: `Bearer ${t}` } : {} }

onMounted(async () => {
  try {
    const [u, s] = await Promise.all([
      fetch(`${API_BASE}/api/users`, { headers: authH() }).then(r => r.ok ? r.json() : []),
      fetch(`${API_BASE}/api/settings`, { headers: authH() }).then(r => r.ok ? r.json() : []),
    ])
    users.value = u
    settings.value = Array.isArray(s) ? s : Object.entries(s).map(([key, value]) => ({ key, value, category: 'general' }))
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Failed to load settings') }
})

async function createUser() {
  try {
    const res = await fetch(`${API_BASE}/api/users`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authH() },
      body: JSON.stringify(userForm.value),
    })
    if (!res.ok) throw new Error(`Create failed: ${res.status}`)
    users.value.push(await res.json())
    showUserForm.value = false
    userForm.value = { username: '', email: '', role: 'USER', password: '' }
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Create user failed') }
}

async function deleteUser(id: string) {
  if (!confirm('Delete this user?')) return
  try {
    await fetch(`${API_BASE}/api/users/${id}`, { method: 'DELETE', headers: authH() })
    users.value = users.value.filter(u => u.id !== id)
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Delete failed') }
}

function roleClass(role: string) {
  return role === 'OWNER' ? 'active' : role === 'ADMIN' ? 'paused' : 'unknown'
}
</script>

<template>
  <header>
    <p>Administration</p>
    <h1>Settings</h1>
  </header>

  <p v-if="app.error" class="error">{{ app.error }}</p>

  <div class="tab-bar">
    <button :class="{ active: activePanel === 'users' }" @click="activePanel = 'users'">Users</button>
    <button :class="{ active: activePanel === 'settings' }" @click="activePanel = 'settings'">System Settings</button>
  </div>

  <!-- Users panel -->
  <template v-if="activePanel === 'users'">
    <section class="panel">
      <div class="panel-header">
        <h2>Users</h2>
        <button v-if="auth.isAdmin" @click="showUserForm = !showUserForm">+ Add User</button>
      </div>

      <form v-if="showUserForm" class="inline-form" @submit.prevent="createUser">
        <input v-model="userForm.username" placeholder="Username" required />
        <input v-model="userForm.email" placeholder="Email" type="email" required />
        <input v-model="userForm.password" placeholder="Password" type="password" required />
        <select v-model="userForm.role">
          <option value="VIEWER">Viewer</option>
          <option value="USER">User</option>
          <option value="ADMIN">Admin</option>
          <option value="OWNER">Owner</option>
        </select>
        <button type="submit">Create</button>
      </form>

      <ul v-if="users.length" class="agent-list" style="margin-top:12px">
        <li v-for="user in users" :key="user.id">
          <div class="agent-row">
            <div>
              <span>{{ user.username }}</span>
              <small>{{ user.email }} · {{ user.id.slice(0, 8) }}</small>
            </div>
            <div class="agent-controls">
              <span class="state-badge" :class="roleClass(user.role)">{{ user.role }}</span>
              <button v-if="auth.isOwner" @click="deleteUser(user.id)">Delete</button>
            </div>
          </div>
        </li>
      </ul>
      <p v-else style="margin-top:12px">No users found.</p>
    </section>
  </template>

  <!-- System settings panel -->
  <template v-if="activePanel === 'settings'">
    <section class="panel">
      <h2>System Settings</h2>
      <ul v-if="settings.length" class="log-list">
        <li v-for="s in settings" :key="s.key">
          <strong>{{ s.key }}</strong>
          <span>{{ s.category }}</span>
          <small>{{ JSON.stringify(s.value) }}</small>
        </li>
      </ul>
      <p v-else>No settings available.</p>
    </section>

    <section class="panel">
      <h2>Session</h2>
      <div class="agent-row">
        <div>
          <span>{{ auth.user?.username }}</span>
          <small>Role: {{ auth.role }}</small>
        </div>
        <div class="agent-controls">
          <button @click="auth.logout()">Sign Out</button>
        </div>
      </div>
    </section>
  </template>
</template>
