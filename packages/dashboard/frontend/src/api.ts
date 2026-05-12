export interface HealthResponse {
  systemName: string;
  version: string;
  status: string;
  echoEnabled: boolean;
  echoDebugOnly: boolean;
  echoActivation: string;
  timestamp: string;
}

export interface AgentDefinition {
  id: string;
  name: string;
  type: string;
  path: string;
  files: string[];
}

export interface AgentRuntime {
  agentId: string;
  state: "ACTIVE" | "PAUSED" | "DISABLED";
  lastActivatedAt: string | null;
  lastDeactivatedAt: string | null;
}

export interface RoutingLog {
  id: string;
  conversationId: string;
  messageId: string;
  decision: string;
  targetAgentId: string | null;
  targetTeamId: string | null;
  targetProjectId: string | null;
  reasoning: string;
  createdAt: string;
}

export interface SystemLog {
  id: string;
  timestamp: string;
  level: string;
  category: string;
  event: string;
  source: string;
  payload: string;
}

const API_BASE = import.meta.env.VITE_API_BASE ?? "";

function authHeaders(): HeadersInit {
  const token = localStorage.getItem("synapse_token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, { headers: authHeaders() });
  if (!res.ok) throw new Error(`GET ${path} failed: ${res.status}`);
  return res.json();
}

async function post<T>(path: string, body?: unknown): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(`POST ${path} failed: ${res.status}`);
  return res.json();
}

async function del(path: string): Promise<void> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  if (!res.ok) throw new Error(`DELETE ${path} failed: ${res.status}`);
}

export const fetchHealth = () => get<HealthResponse>("/api/health");
export const fetchAgents = () => get<AgentDefinition[]>("/api/agents");
export const fetchAgentRuntimes = () =>
  get<AgentRuntime[]>("/api/agents/runtime");
export const activateAgent = (id: string) =>
  post<AgentRuntime>(`/api/agents/${id}/activate`);
export const pauseAgent = (id: string) =>
  post<AgentRuntime>(`/api/agents/${id}/pause`);
export const disableAgent = (id: string) =>
  post<AgentRuntime>(`/api/agents/${id}/disable`);
export const fetchRoutingLogs = () => get<RoutingLog[]>("/api/agents/routing");

export interface Plugin {
  id: string;
  name: string;
  type: string;
  version: string;
  status: string;
  createdAt: string;
}

export interface StoreEntry {
  id: string;
  name: string;
  type: string;
  source: string;
  version: string;
  author: string | null;
  license: string | null;
  description: string | null;
  tags: string[] | null;
  minSynapse: string | null;
}

export const fetchPlugins = () => get<Plugin[]>("/api/plugins");
export const enablePlugin = (id: string) =>
  post<Plugin>(`/api/plugins/${id}/enable`);
export const disablePlugin = (id: string) =>
  post<Plugin>(`/api/plugins/${id}/disable`);
export const uninstallPlugin = (id: string) => del(`/api/plugins/${id}`);
export const fetchStore = (type?: "PLUGIN" | "BUNDLE") =>
  get<StoreEntry[]>(type ? `/api/store?type=${type}` : "/api/store");
export const fetchStoreEntry = (id: string) =>
  get<StoreEntry>(`/api/store/${id}`);
export const installBundle = (id: string) =>
  post<{ success: boolean; installed: string[]; errors: string[] }>(
    `/api/store/${id}/install`,
  );
export const validateBundle = (id: string) =>
  post<{ valid: boolean; errors: string[] }>(`/api/store/${id}/validate`);

export const fetchLogs = (limit = 25) =>
  get<SystemLog[]>(`/api/logs?limit=${limit}`);

export interface ConversationStreamEvent {
  id: string;
  type: string;
  source: string;
  payload: Record<string, unknown>;
  correlationId: string | null;
  occurredAt: string;
}

export function connectConversationStream(
  onEvent: (event: ConversationStreamEvent) => void,
  onOpen?: () => void,
  onClose?: () => void,
): WebSocket {
  const ws = new WebSocket(
    `${API_BASE.replace(/^http/, "ws")}/ws/conversations`,
  );
  ws.onmessage = (e: MessageEvent) => {
    try {
      onEvent(JSON.parse(e.data));
    } catch {
      /* ignore */
    }
  };
  if (onOpen) ws.onopen = onOpen;
  if (onClose) ws.onclose = onClose;
  return ws;
}

export interface LiveLogEvent {
  id: string;
  type: string;
  source: string;
  payload: Record<string, string>;
  occurredAt: string;
}

export function connectLogStream(
  onEvent: (event: LiveLogEvent) => void,
  onError?: (err: Event) => void,
): EventSource {
  const es = new EventSource(`${API_BASE}/api/logs/stream`);
  es.addEventListener("log", (e: MessageEvent) => {
    try {
      onEvent(JSON.parse(e.data));
    } catch {
      // ignore malformed events
    }
  });
  if (onError) es.onerror = onError;
  return es;
}
