<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAppStore } from '../stores/appStore'
import { useLiveStore } from '../stores/liveStore'

interface Conversation {
  id: string
  agentId: string
  userId: string
  status: string
  startedAt: string
}

interface Message {
  id: string
  conversationId: string
  role: string
  content: string
  tokens: number | null
  createdAt: string
}

const app = useAppStore()
const live = useLiveStore()
const conversations = ref<Conversation[]>([])
const selectedConv = ref<Conversation | null>(null)
const messages = ref<Message[]>([])
const newMessage = ref('')
const sending = ref(false)

const API_BASE = import.meta.env.VITE_API_BASE ?? ''
function auth(): Record<string, string> {
  const t = localStorage.getItem('synapse_token')
  return t ? { Authorization: `Bearer ${t}` } : {}
}

onMounted(load)

async function load() {
  try {
    const res = await fetch(`${API_BASE}/api/conversations`, { headers: auth() })
    if (res.ok) conversations.value = await res.json()
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Failed to load conversations') }
}

async function selectConversation(conv: Conversation) {
  selectedConv.value = conv
  try {
    const res = await fetch(`${API_BASE}/api/conversations/${conv.id}/messages`, { headers: auth() })
    if (res.ok) messages.value = await res.json()
  } catch { messages.value = [] }
}

async function sendMessage() {
  if (!selectedConv.value || !newMessage.value.trim()) return
  sending.value = true
  try {
    const res = await fetch(`${API_BASE}/api/conversations/${selectedConv.value.id}/messages`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...auth() },
      body: JSON.stringify({ content: newMessage.value }),
    })
    if (res.ok) {
      newMessage.value = ''
      await selectConversation(selectedConv.value)
    }
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Send failed') }
  finally { sending.value = false }
}

async function createConversation() {
  try {
    const res = await fetch(`${API_BASE}/api/conversations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...auth() },
      body: JSON.stringify({ agentId: 'main-agent' }),
    })
    if (res.ok) {
      const conv = await res.json()
      conversations.value.unshift(conv.conversation ?? conv)
    }
  } catch (e) { app.setError(e instanceof Error ? e.message : 'Create failed') }
}
</script>

<template>
  <header>
    <p>Chat Runtime</p>
    <h1>Conversations</h1>
  </header>

  <p v-if="app.error" class="error">{{ app.error }}</p>

  <div class="conv-layout">
    <section class="panel conv-list">
      <div class="panel-header">
        <h2>Conversations</h2>
        <button @click="createConversation">+ New</button>
      </div>
      <ul class="agent-list">
        <li
          v-for="conv in conversations"
          :key="conv.id"
          :class="{ 'conv-selected': selectedConv?.id === conv.id }"
          @click="selectConversation(conv)"
          style="cursor:pointer"
        >
          <span>{{ conv.agentId }}</span>
          <small>{{ conv.status }} · {{ new Date(conv.startedAt).toLocaleString() }}</small>
        </li>
      </ul>
      <p v-if="!conversations.length">No conversations yet.</p>
    </section>

    <section class="panel conv-messages" v-if="selectedConv">
      <h2>Messages — {{ selectedConv.id.slice(0, 8) }}</h2>
      <div class="message-list">
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['message', msg.role.toLowerCase()]"
        >
          <small>{{ msg.role }}</small>
          <p>{{ msg.content }}</p>
        </div>
        <div v-if="!messages.length" style="color:#aab4bf">No messages yet.</div>
      </div>
      <form class="message-form" @submit.prevent="sendMessage">
        <input v-model="newMessage" placeholder="Send a message…" :disabled="sending" />
        <button type="submit" :disabled="sending">{{ sending ? '…' : 'Send' }}</button>
      </form>
    </section>
    <section class="panel" v-else>
      <p style="color:#aab4bf">Select a conversation to view messages.</p>
    </section>
  </div>

  <section class="panel" style="margin-top:0">
    <h2>
      Live Events
      <span class="state-badge" :class="live.wsConnected ? 'active' : 'disabled'">
        {{ live.wsConnected ? 'LIVE' : 'OFFLINE' }}
      </span>
    </h2>
    <ul v-if="live.conversationEvents.length" class="log-list">
      <li v-for="event in live.conversationEvents.slice(0, 20)" :key="event.id">
        <strong>{{ event.type }}</strong>
        <span>{{ event.source }}</span>
        <small>{{ new Date(event.occurredAt).toLocaleTimeString() }}</small>
      </li>
    </ul>
    <p v-else style="color:#aab4bf">Waiting for conversation events…</p>
  </section>
</template>
