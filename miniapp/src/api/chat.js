export function getHistory(characterId) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `http://localhost:8080/api/chat/${characterId}`,
      method: 'GET',
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          reject(new Error(res.data?.message || `HTTP ${res.statusCode}`))
        }
      },
      fail: (err) => reject(err)
    })
  })
}

export function clearHistory(characterId) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `http://localhost:8080/api/chat/${characterId}`,
      method: 'DELETE',
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          reject(new Error(res.data?.message || `HTTP ${res.statusCode}`))
        }
      },
      fail: (err) => reject(err)
    })
  })
}

export function sendMessage(characterId, message, modelConfigId, onChunk, onDone, onError) {
  const task = uni.request({
    url: 'http://localhost:8080/api/chat',
    method: 'POST',
    enableChunked: true,
    data: { characterId, message, modelConfigId },
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
      if (line.startsWith('event:chunk')) {
        continue
      }
      if (line.startsWith('data:')) {
        const data = line.substring(5).trim()
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
      } else if (line.startsWith('event:done')) {
        if (onDone) onDone()
      } else if (line.startsWith('event:error')) {
        if (onError) onError(new Error('Stream error'))
      }
    }
  })

  return task
}
