import http from './request'

export const getHistory = (characterId) => http.get(`/chat/${characterId}`)
export const clearHistory = (characterId) => http.delete(`/chat/${characterId}`)

export async function* sendMessage(characterId, message, modelConfigId) {
  const response = await fetch('/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ characterId, message, modelConfigId })
  })
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''
    for (const line of lines) {
      if (line.startsWith('data:') && !line.includes('event:')) {
        const data = line.substring(5).trim()
        if (data && data !== '[DONE]') {
          try {
            const parsed = JSON.parse(data)
            yield { type: 'chunk', content: typeof parsed === 'string' ? parsed : data }
          } catch {
            yield { type: 'chunk', content: data }
          }
        }
      } else if (line.startsWith('event:done') || line.includes('event:done')) {
        yield { type: 'done' }
        return
      } else if (line.startsWith('event:error') || line.includes('event:error')) {
        yield { type: 'error', message: 'Stream error' }
        return
      }
    }
  }
}

export const regenerateMessage = async function* (characterId, modelConfigId) {
  // Same SSE pattern for regenerate
  const response = await fetch('/api/chat/regenerate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ characterId, modelConfigId })
  })
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''
    for (const line of lines) {
      if (line.startsWith('data:') && !line.includes('event:')) {
        const data = line.substring(5).trim()
        if (data && data !== '[DONE]') yield { type: 'chunk', content: data }
      } else if (line.includes('event:done')) {
        yield { type: 'done' }; return
      } else if (line.includes('event:error')) {
        yield { type: 'error', message: 'Error' }; return
      }
    }
  }
}

export const getSuggestions = (characterId) => http.post(`/chat/${characterId}/suggestions`)
