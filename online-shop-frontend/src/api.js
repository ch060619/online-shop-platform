import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuth, getToken } from './auth'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && payload.code && payload.code !== 200) {
      ElMessage.error(payload.message || '请求失败')
      if (payload.code === 401) {
        clearAuth()
        const redirect = encodeURIComponent(`${window.location.pathname}${window.location.search}`)
        if (!window.location.pathname.startsWith('/login')) {
          window.location.href = `/login?redirect=${redirect}`
        }
      }
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return payload ? payload.data : response.data
  },
  (error) => {
    ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export const authApi = {
  login: (data) => http.post('/auth/login', data)
}

export const productApi = {
  list: (params) => http.get('/products', { params }),
  detail: (id) => http.get(`/products/${id}`)
}

export const cartApi = {
  get: () => http.get('/cart'),
  add: (data) => http.post('/cart/items', data),
  update: (id, data) => http.put(`/cart/items/${id}`, data),
  remove: (id) => http.delete(`/cart/items/${id}`)
}

export const orderApi = {
  create: (data) => http.post('/orders', data),
  list: () => http.get('/orders'),
  detail: (id) => http.get(`/orders/${id}`),
  cancel: (id) => http.put(`/orders/${id}/cancel`)
}
