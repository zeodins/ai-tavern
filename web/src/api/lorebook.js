import http from './request'
export const listLorebook = (characterId) => http.get(`/characters/${characterId}/lorebook`)
export const createLorebookEntry = (characterId, data) => http.post(`/characters/${characterId}/lorebook`, data)
export const updateLorebookEntry = (id, data) => http.put(`/lorebook/${id}`, data)
export const deleteLorebookEntry = (id) => http.delete(`/lorebook/${id}`)
