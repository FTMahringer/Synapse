export interface HealthResponse {
  systemName: string
  version: string
  status: string
  echoEnabled: boolean
  echoDebugOnly: boolean
  echoActivation: string
  timestamp: string
}

export interface AgentDefinition {
  id: string
  name: string
  type: string
  path: string
  files: string[]
}

export interface AgentRuntime {
  agentId: string
  state: 'ACTIVE' | 'PAUSED' | 'DISABLED'
  lastActivatedAt: string | null
  lastDeactivatedAt: string | null
}

export interface RoutingLog {
  id: string
  conversationId: string
  messageId: string
  decision: string
  targetAgentId: string | null
  targetTeamId: string | null
  targetProjectId: string | null
  reasoning: string
  createdAt: string
}

export interface SystemLog {
  id: string
  timestamp: string
  level: string
  category: string
  event: string
  source: string
  payload: string
}

const API_BASE = import.meta.env.VITE_API_BASE ?? ''

export async function fetchHealth(): Promise<HealthResponse> {
  const response = await fetch(`${API_BASE}/api/health`)
  if (!response.ok) throw new Error(`Health request failed: ${response.status}`)
  return response.json()
}

export async function fetchAgents(): Promise<AgentDefinition[]> {
  const response = await fetch(`${API_BASE}/api/agents`)
  if (!response.ok) throw new Error(`Agents request failed: ${response.status}`)
  return response.json()
}

export async function fetchAgentRuntimes(): Promise<AgentRuntime[]> {
  const response = await fetch(`${API_BASE}/api/agents/runtime`)
  if (!response.ok) throw new Error(`Agent runtimes request failed: ${response.status}`)
  return response.json()
}

export async function activateAgent(agentId: string): Promise<AgentRuntime> {
  const response = await fetch(`${API_BASE}/api/agents/${agentId}/activate`, { method: 'POST' })
  if (!response.ok) throw new Error(`Activate failed: ${response.status}`)
  return response.json()
}

export async function pauseAgent(agentId: string): Promise<AgentRuntime> {
  const response = await fetch(`${API_BASE}/api/agents/${agentId}/pause`, { method: 'POST' })
  if (!response.ok) throw new Error(`Pause failed: ${response.status}`)
  return response.json()
}

export async function disableAgent(agentId: string): Promise<AgentRuntime> {
  const response = await fetch(`${API_BASE}/api/agents/${agentId}/disable`, { method: 'POST' })
  if (!response.ok) throw new Error(`Disable failed: ${response.status}`)
  return response.json()
}

export async function fetchRoutingLogs(): Promise<RoutingLog[]> {
  const response = await fetch(`${API_BASE}/api/agents/routing`)
  if (!response.ok) throw new Error(`Routing logs request failed: ${response.status}`)
  return response.json()
}

export interface Plugin {
  id: string
  name: string
  type: string
  version: string
  status: string
  createdAt: string
}

export interface StoreEntry {
  id: string
  name: string
  type: string
  source: string
  version: string
  author: string | null
  description: string | null
  tags: string[] | null
}

export async function fetchPlugins(): Promise<Plugin[]> {
  const response = await fetch(`${API_BASE}/api/plugins`)
  if (!response.ok) throw new Error(`Plugins request failed: ${response.status}`)
  return response.json()
}

export async function enablePlugin(id: string): Promise<Plugin> {
  const response = await fetch(`${API_BASE}/api/plugins/${id}/enable`, { method: 'POST' })
  if (!response.ok) throw new Error(`Enable plugin failed: ${response.status}`)
  return response.json()
}

export async function disablePlugin(id: string): Promise<Plugin> {
  const response = await fetch(`${API_BASE}/api/plugins/${id}/disable`, { method: 'POST' })
  if (!response.ok) throw new Error(`Disable plugin failed: ${response.status}`)
  return response.json()
}

export async function uninstallPlugin(id: string): Promise<void> {
  const response = await fetch(`${API_BASE}/api/plugins/${id}`, { method: 'DELETE' })
  if (!response.ok) throw new Error(`Uninstall plugin failed: ${response.status}`)
}

export async function fetchStore(type?: 'PLUGIN' | 'BUNDLE'): Promise<StoreEntry[]> {
  const url = type ? `${API_BASE}/api/store?type=${type}` : `${API_BASE}/api/store`
  const response = await fetch(url)
  if (!response.ok) throw new Error(`Store request failed: ${response.status}`)
  return response.json()
}

export async function installBundle(id: string): Promise<{ success: boolean; installed: string[]; errors: string[] }> {
  const response = await fetch(`${API_BASE}/api/store/${id}/install`, { method: 'POST' })
  if (!response.ok) throw new Error(`Bundle install failed: ${response.status}`)
  return response.json()
}

export async function fetchLogs(limit = 25): Promise<SystemLog[]> {
  const response = await fetch(`${API_BASE}/api/logs?limit=${limit}`)
  if (!response.ok) throw new Error(`Logs request failed: ${response.status}`)
  return response.json()
}

export interface ConversationStreamEvent {
  id: string
  type: string
  source: string
  payload: Record<string, unknown>
  correlationId: string | null
  occurredAt: string
}

export function connectConversationStream(
  onEvent: (event: ConversationStreamEvent) => void,
  onOpen?: () => void,
  onClose?: () => void
): WebSocket {
  const ws = new WebSocket(`${API_BASE.replace(/^http/, 'ws')}/ws/conversations`)
  ws.onmessage = (e: MessageEvent) => {
    try { onEvent(JSON.parse(e.data)) } catch { /* ignore */ }
  }
  if (onOpen) ws.onopen = onOpen
  if (onClose) ws.onclose = onClose
  return ws
}

export interface LiveLogEvent {
  id: string
  type: string
  source: string
  payload: Record<string, string>
  occurredAt: string
}

export function connectLogStream(
  onEvent: (event: LiveLogEvent) => void,
  onError?: (err: Event) => void
): EventSource {
  const es = new EventSource(`${API_BASE}/api/logs/stream`)
  es.addEventListener('log', (e: MessageEvent) => {
    try {
      onEvent(JSON.parse(e.data))
    } catch {
      // ignore malformed events
    }
  })
  if (onError) es.onerror = onError
  return es
}
