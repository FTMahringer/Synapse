<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { RouterView, RouterLink } from 'vue-router'
import { connectWithBackoff, connectWsWithBackoff } from './reconnect'
import { useLiveStore } from './stores/liveStore'
import { useAuthStore } from './stores/authStore'
import type { ConversationStreamEvent, LiveLogEvent } from './api'

const live = useLiveStore()
const auth = useAuthStore()
const API_BASE = import.meta.env.VITE_API_BASE ?? ''

const loginUsername = ref('')
const loginPassword = ref('')

async function doLogin() {
  await auth.login(loginUsername.value, loginPassword.value)
}

let stopLogStream: (() => void) | null = null
let stopWsStream: (() => void) | null = null

onMounted(() => {
  stopLogStream = connectWithBackoff(
    `${API_BASE}/api/logs/stream`,
    'log',
    (data) => {
      try { live.addLog(JSON.parse(data) as LiveLogEvent) } catch { /* ignore */ }
    },
    { onFallback: () => { live.sseConnected = false } }
  )

  stopWsStream = connectWsWithBackoff(
    `${API_BASE.replace(/^http/, 'ws')}/ws/conversations`,
    (data) => {
      try { live.addConversationEvent(JSON.parse(data) as ConversationStreamEvent) } catch { /* ignore */ }
    },
    () => { live.wsConnected = true },
    () => { live.wsConnected = false }
  )
})

onUnmounted(() => {
  stopLogStream?.()
  stopWsStream?.()
})
</script>

<template>
  <!-- Login overlay when not authenticated -->
  <div v-if="!auth.isAuthenticated" class="login-overlay">
    <div class="login-card">
      <div class="brand">SYNAPSE</div>
      <p class="login-sub">Operator Console</p>
      <form @submit.prevent="doLogin">
        <input v-model="loginUsername" type="text" placeholder="Username" autocomplete="username" />
        <input v-model="loginPassword" type="password" placeholder="Password" autocomplete="current-password" />
        <p v-if="auth.loginError" class="error">{{ auth.loginError }}</p>
        <button type="submit">Sign In</button>
      </form>
    </div>
  </div>

  <!-- Main shell when authenticated -->
  <main v-else class="shell">
    <aside class="sidebar">
      <div class="brand">SYNAPSE</div>
      <nav>
        <RouterLink to="/overview">Overview</RouterLink>
        <RouterLink to="/agents">Agents</RouterLink>
        <RouterLink to="/providers">Providers</RouterLink>
        <RouterLink to="/conversations">Conversations</RouterLink>
        <RouterLink to="/plugins">Plugins</RouterLink>
        <RouterLink to="/store">Store</RouterLink>
        <RouterLink to="/routing">Routing</RouterLink>
        <RouterLink to="/logs">Logs</RouterLink>
        <RouterLink to="/settings">Settings</RouterLink>
      </nav>
      <div class="stream-status">
        <span class="state-badge" :class="live.sseConnected ? 'active' : 'disabled'">SSE</span>
        <span class="state-badge" :class="live.wsConnected ? 'active' : 'disabled'">WS</span>
        <span class="role-badge">{{ auth.role }}</span>
        <button class="logout-btn" @click="auth.logout()">Out</button>
      </div>
    </aside>

    <section class="content">
      <RouterView />
    </section>
  </main>
</template>
