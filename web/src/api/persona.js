import http from './request'
export const listPersonas = () => http.get('/personas')
export const getPersona = (id) => http.get(`/personas/${id}`)
export const createPersona = (data) => http.post('/personas', data)
export const updatePersona = (id, data) => http.put(`/personas/${id}`, data)
export const deletePersona = (id) => http.delete(`/personas/${id}`)
export const activatePersona = (id) => http.put(`/personas/${id}/activate`)
