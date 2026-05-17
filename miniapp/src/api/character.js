import request, { uploadFile } from './request'

export function listCharacters() {
  return request({ url: '/characters' })
}

export function getCharacter(id) {
  return request({ url: `/characters/${id}` })
}

export function importCharacter(filePath, modelConfigId) {
  const formData = {}
  if (modelConfigId) {
    formData.modelConfigId = String(modelConfigId)
  }
  return uploadFile('/characters', filePath, formData)
}

export function updateCharacter(id, data) {
  return request({
    url: `/characters/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteCharacter(id) {
  return request({
    url: `/characters/${id}`,
    method: 'DELETE'
  })
}

export function applyPreset(characterId, presetId) {
  return request({ url: `/characters/${characterId}/apply-preset/${presetId}`, method: 'PUT' })
}
