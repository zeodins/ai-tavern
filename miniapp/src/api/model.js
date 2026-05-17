import request from './request'

export function listModels() {
  return request({ url: '/models' })
}

export function getModel(id) {
  return request({ url: `/models/${id}` })
}

export function createModel(data) {
  return request({
    url: '/models',
    method: 'POST',
    data
  })
}

export function updateModel(id, data) {
  return request({
    url: `/models/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteModel(id) {
  return request({
    url: `/models/${id}`,
    method: 'DELETE'
  })
}
