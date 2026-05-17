import request from './request'

export function listPresets() { return request({ url: '/presets' }) }
export function getPreset(id) { return request({ url: `/presets/${id}` }) }
export function createPreset(data) { return request({ url: '/presets', method: 'POST', data }) }
export function updatePreset(id, data) { return request({ url: `/presets/${id}`, method: 'PUT', data }) }
export function deletePreset(id) { return request({ url: `/presets/${id}`, method: 'DELETE' }) }
