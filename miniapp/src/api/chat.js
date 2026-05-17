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

export function regenerateMessage(characterId, modelConfigId, onChunk, onDone, onError) {
  const task = uni.request({
    url: 'http://localhost:8080/api/chat/regenerate',
    method: 'POST',
    enableChunked: true,
    data: { characterId, modelConfigId },
    success: () => {},
    fail: (err) => {
      if (onError) onError(err)
    }
  })

  let buffer = ''

  task.onChunkReceived((chunk) => {
    const text = new TextDecoder().decode(chunk.data)
    buffer += text

    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      if (line.startsWith('event:chunk')) continue
      if (line.startsWith('data:')) {
        const data = line.substring(5).trim()
        if (!data) continue
        try {
          const parsed = JSON.parse(data)
          if (parsed && typeof parsed === 'string') {
            onChunk(parsed)
          } else {
            onChunk(data)
          }
        } catch {
          onChunk(data)
        }
      } else if (line.startsWith('event:done')) {
        if (onDone) onDone()
      } else if (line.startsWith('event:error')) {
        if (onError) onError(new Error('Regenerate error'))
      }
    }
  })

  return task
}

export function getSuggestions(characterId) {
  return new Promise((resolve) => {
    uni.request({
      url: `http://localhost:8080/api/chat/${characterId}/suggestions`,
      method: 'POST',
      data: {},
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          resolve([])
        }
      },
      fail: () => resolve([])
    })
  })
}
