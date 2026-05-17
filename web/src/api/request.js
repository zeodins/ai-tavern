import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

export function uploadFile(url, file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post(url, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export default http
