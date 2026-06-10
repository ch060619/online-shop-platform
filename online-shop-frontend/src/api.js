import request from './request'

const cleanParams = (params = {}) => Object.fromEntries(
  Object.entries(params).filter(([, value]) => value !== '' && value !== undefined && value !== null)
)

export const authApi = {
  login: (data) => request.post('/auth/login', data)
}

export const productApi = {
  list: (params) => request.get('/products', { params: cleanParams(params) }),
  detail: (id) => request.get(`/products/${id}`)
}

export const cartApi = {
  get: () => request.get('/cart'),
  add: (data) => request.post('/cart/items', data),
  update: (id, data) => request.put(`/cart/items/${id}`, data),
  remove: (id) => request.delete(`/cart/items/${id}`)
}

export const orderApi = {
  create: (data) => request.post('/orders', data),
  list: () => request.get('/orders'),
  detail: (id) => request.get(`/orders/${id}`),
  cancel: (id) => request.put(`/orders/${id}/cancel`)
}
