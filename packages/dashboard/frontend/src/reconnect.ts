/**
 * Reconnecting EventSource with exponential backoff.
 * Falls back to polling if SSE fails after maxRetries.
 */
export interface ReconnectOptions {
  initialDelay?: number
  maxDelay?: number
  maxRetries?: number
  onFallback?: () => void
}

export function connectWithBackoff(
  url: string,
  eventName: string,
  onMessage: (data: string) => void,
  options: ReconnectOptions = {}
): () => void {
  const { initialDelay = 1000, maxDelay = 30000, maxRetries = 10, onFallback } = options

  let es: EventSource | null = null
  let retries = 0
  let delay = initialDelay
  let stopped = false
  let retryTimer: ReturnType<typeof setTimeout> | null = null

  function connect() {
    if (stopped) return
    es = new EventSource(url)

    es.addEventListener(eventName, (e: MessageEvent) => {
      retries = 0
      delay = initialDelay
      onMessage(e.data)
    })

    es.onerror = () => {
      es?.close()
      es = null
      if (stopped) return

      retries++
      if (retries > maxRetries) {
        onFallback?.()
        return
      }

      retryTimer = setTimeout(() => {
        delay = Math.min(delay * 2, maxDelay)
        connect()
      }, delay)
    }
  }

  connect()

  return () => {
    stopped = true
    if (retryTimer) clearTimeout(retryTimer)
    es?.close()
  }
}

/**
 * Reconnecting WebSocket with exponential backoff.
 */
export function connectWsWithBackoff(
  url: string,
  onMessage: (data: string) => void,
  onOpen?: () => void,
  onClose?: () => void,
  options: ReconnectOptions = {}
): () => void {
  const { initialDelay = 1000, maxDelay = 30000, maxRetries = 10 } = options

  let ws: WebSocket | null = null
  let retries = 0
  let delay = initialDelay
  let stopped = false
  let retryTimer: ReturnType<typeof setTimeout> | null = null

  function connect() {
    if (stopped) return
    ws = new WebSocket(url)

    ws.onopen = () => {
      retries = 0
      delay = initialDelay
      onOpen?.()
    }

    ws.onmessage = (e: MessageEvent) => onMessage(e.data)

    ws.onclose = () => {
      onClose?.()
      if (stopped) return

      retries++
      if (retries > maxRetries) return

      retryTimer = setTimeout(() => {
        delay = Math.min(delay * 2, maxDelay)
        connect()
      }, delay)
    }
  }

  connect()

  return () => {
    stopped = true
    if (retryTimer) clearTimeout(retryTimer)
    ws?.close()
  }
}
