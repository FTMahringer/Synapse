import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const error = ref<string | null>(null)
  const loading = ref(false)

  function setError(msg: string | null) {
    error.value = msg
  }

  function clearError() {
    error.value = null
  }

  function setLoading(val: boolean) {
    loading.value = val
  }

  return { error, loading, setError, clearError, setLoading }
})
