import http from './request'
export const listModels = () => http.get('/models')
export const getModel = (id) => http.get(`/models/${id}`)
export const createModel = (data) => http.post('/models', data)
export const updateModel = (id, data) => http.put(`/models/${id}`, data)
export const deleteModel = (id) => http.delete(`/models/${id}`)
export const activateModel = (id) => http.put(`/models/${id}/activate`)
