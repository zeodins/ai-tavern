import request from './request'

const BASE_URL = 'http://localhost:8080/api'

export function getHistory(characterId) {
  return request({
    url: `/chat/${characterId}`,
    method: 'GET'
  })
}

export function clearHistory(characterId) {
  return request({
    url: `/chat/${characterId}`,
    method: 'DELETE'
  })
}

export function sendMessage(characterId, message, modelConfigId, onChunk, onDone, onError) {
  const task = uni.request({
    url: BASE_URL + '/chat',
    method: 'POST',
    enableChunked: true,
    data: { characterId, message, modelConfigId },
    success: () => {},
    fail: (err) => {
      if (onError) onError(err)
    }
  })

  let buffer = ''
  let errorFlag = false

  task.onChunkReceived((chunk) => {
    const text = new TextDecoder().decode(chunk.data)
    buffer += text

    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      if (line.startsWith('event:chunk')) {
        continue
      }
      if (line.startsWith('event:error')) {
        errorFlag = true
        continue
      }
      if (line.startsWith('event:done')) {
        if (onDone) onDone()
        continue
      }
      if (line.startsWith('data:')) {
        const data = line.substring(5).trim()
        if (errorFlag) {
          errorFlag = false
          if (onError) onError(new Error(data || 'Stream error'))
          continue
        }
        if (!data) continue
        try {
          const parsed = JSON.parse(data)
          if (parsed && typeof parsed === 'string') {
            onChunk(parsed)
          } else if (parsed && parsed.content) {
            onChunk(parsed.content)
          } else {
            onChunk(data)
          }
        } catch {
          onChunk(data)
        }
      }
    }
  })

  return task
}
