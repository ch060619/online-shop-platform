import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuth, getToken } from './auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

const firstFieldError = (data) => {
  if (!data || typeof data !== 'object' || Array.isArray(data)) {
    return ''
  }
  const [, message] = Object.entries(data)[0] || []
  return message || ''
}

const errorMessage = (payload, fallback = '请求失败') => {
  if (!payload) {
    return fallback
  }
  return firstFieldError(payload.data) || payload.message || fallback
}

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && payload.code && payload.code !== 200) {
      const message = errorMessage(payload)
      ElMessage.error(message)
      if (payload.code === 401) {
        clearAuth()
        const redirect = encodeURIComponent(`${window.location.pathname}${window.location.search}`)
        if (!window.location.pathname.startsWith('/login')) {
          window.location.href = `/login?redirect=${redirect}`
        }
      }
      return Promise.reject(new Error(message))
    }
    return payload ? payload.data : response.data
  },
  (error) => {
    ElMessage.error(errorMessage(error.response?.data, error.message || '网络异常'))
    return Promise.reject(error)
  }
)

export default request
