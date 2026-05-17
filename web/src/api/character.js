import http, { uploadFile } from './request'

export const listCharacters = () => http.get('/characters')
export const getCharacter = (id) => http.get(`/characters/${id}`)
export const importCharacter = (file, modelConfigId) => {
  const formData = new FormData()
  formData.append('file', file)
  if (modelConfigId) formData.append('modelConfigId', modelConfigId)
  return http.post('/characters', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}
export const updateCharacter = (id, data) => http.put(`/characters/${id}`, data)
export const deleteCharacter = (id) => http.delete(`/characters/${id}`)
export const applyPreset = (characterId, presetId) => http.put(`/characters/${characterId}/apply-preset/${presetId}`)
