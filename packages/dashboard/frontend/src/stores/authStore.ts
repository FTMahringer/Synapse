import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export type UserRole = 'OWNER' | 'ADMIN' | 'USER' | 'VIEWER'

export interface AuthUser {
  username: string
  role: UserRole
  token: string
}

const TOKEN_KEY = 'synapse_token'
const USER_KEY = 'synapse_user'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<AuthUser | null>(loadPersistedUser())
  const loginError = ref<string | null>(null)

  const isAuthenticated = computed(() => user.value !== null)
  const role = computed(() => user.value?.role ?? null)
  const token = computed(() => user.value?.token ?? null)

  const isOwner = computed(() => role.value === 'OWNER')
  const isAdmin = computed(() => role.value === 'OWNER' || role.value === 'ADMIN')
  const canWrite = computed(() => role.value === 'OWNER' || role.value === 'ADMIN' || role.value === 'USER')

  async function login(username: string, password: string): Promise<boolean> {
    loginError.value = null
    try {
      const res = await fetch(`${apiBase()}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      })
      if (!res.ok) {
        loginError.value = res.status === 401 ? 'Invalid credentials' : `Login failed (${res.status})`
        return false
      }
      const data = await res.json()
      user.value = {
        username: data.username ?? username,
        role: data.role ?? 'USER',
        token: data.token ?? data.accessToken,
      }
      localStorage.setItem(TOKEN_KEY, user.value.token)
      localStorage.setItem(USER_KEY, JSON.stringify(user.value))
      return true
    } catch {
      loginError.value = 'Cannot reach backend'
      return false
    }
  }

  function logout() {
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { user, loginError, isAuthenticated, role, token, isOwner, isAdmin, canWrite, login, logout }
})

function loadPersistedUser(): AuthUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch { return null }
}

function apiBase(): string {
  return (import.meta as any).env?.VITE_API_BASE ?? ''
}
