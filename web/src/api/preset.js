import http from './request'
export const listPresets = () => http.get('/presets')
export const getPreset = (id) => http.get(`/presets/${id}`)
export const createPreset = (data) => http.post('/presets', data)
export const updatePreset = (id, data) => http.put(`/presets/${id}`, data)
export const deletePreset = (id) => http.delete(`/presets/${id}`)
export const togglePreset = (id) => http.put(`/presets/${id}/toggle`)
