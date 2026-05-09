import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ConversationStreamEvent, LiveLogEvent } from '../api'

export const useLiveStore = defineStore('live', () => {
  const liveLogs = ref<LiveLogEvent[]>([])
  const conversationEvents = ref<ConversationStreamEvent[]>([])
  const sseConnected = ref(false)
  const wsConnected = ref(false)

  function addLog(event: LiveLogEvent) {
    liveLogs.value.unshift(event)
    if (liveLogs.value.length > 200) liveLogs.value.pop()
    sseConnected.value = true
  }

  function addConversationEvent(event: ConversationStreamEvent) {
    conversationEvents.value.unshift(event)
    if (conversationEvents.value.length > 100) conversationEvents.value.pop()
  }

  return {
    liveLogs, conversationEvents, sseConnected, wsConnected,
    addLog, addConversationEvent,
  }
})
