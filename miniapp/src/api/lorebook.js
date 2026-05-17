import request from './request'

export function listLorebook(characterId) {
    return request({ url: `/characters/${characterId}/lorebook` })
}

export function createLorebookEntry(characterId, data) {
    return request({ url: `/characters/${characterId}/lorebook`, method: 'POST', data })
}

export function updateLorebookEntry(id, data) {
    return request({ url: `/lorebook/${id}`, method: 'PUT', data })
}

export function deleteLorebookEntry(id) {
    return request({ url: `/lorebook/${id}`, method: 'DELETE' })
}
