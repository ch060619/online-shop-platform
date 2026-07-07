import request from './request'

const cleanParams = (params = {}) => Object.fromEntries(
  Object.entries(params).filter(([, value]) => value !== '' && value !== undefined && value !== null)
)

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data)
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
  create: (data) => request.post('/orders', data, {
    headers: { 'Idempotency-Key': crypto.randomUUID() }
  }),
  list: () => request.get('/orders'),
  detail: (id) => request.get(`/orders/${id}`),
  cancel: (id) => request.put(`/orders/${id}/cancel`),
  pay: (id) => request.put(`/orders/${id}/pay`)
}

export const addressApi = {
  list: () => request.get('/addresses'),
  add: (data) => request.post('/addresses', data),
  update: (id, data) => request.put(`/addresses/${id}`, data),
  remove: (id) => request.delete(`/addresses/${id}`)
}

export const userApi = {
  profile: () => request.get('/users/me'),
  changePassword: (data) => request.put('/users/me/password', data)
}
