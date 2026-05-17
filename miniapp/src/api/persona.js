import request from './request'

export function listPersonas() { return request({ url: '/personas' }) }
export function getPersona(id) { return request({ url: `/personas/${id}` }) }
export function createPersona(data) { return request({ url: '/personas', method: 'POST', data }) }
export function updatePersona(id, data) { return request({ url: `/personas/${id}`, method: 'PUT', data }) }
export function deletePersona(id) { return request({ url: `/personas/${id}`, method: 'DELETE' }) }
export function activatePersona(id) { return request({ url: `/personas/${id}/activate`, method: 'PUT' }) }
