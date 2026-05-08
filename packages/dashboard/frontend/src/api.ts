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
  if (!response.ok) {
    throw new Error(`Health request failed: ${response.status}`)
  }
  return response.json()
}

export async function fetchAgents(): Promise<AgentDefinition[]> {
  const response = await fetch(`${API_BASE}/api/agents`)
  if (!response.ok) {
    throw new Error(`Agents request failed: ${response.status}`)
  }
  return response.json()
}

export async function fetchLogs(limit = 25): Promise<SystemLog[]> {
  const response = await fetch(`${API_BASE}/api/logs?limit=${limit}`)
  if (!response.ok) {
    throw new Error(`Logs request failed: ${response.status}`)
  }
  return response.json()
}
