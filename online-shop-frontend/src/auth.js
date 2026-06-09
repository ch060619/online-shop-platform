import { reactive } from 'vue'

const TOKEN_KEY = 'online-shop-token'
const USER_KEY = 'online-shop-user'

const readUser = () => {
  const value = localStorage.getItem(USER_KEY)
  if (!value) {
    return null
  }
  try {
    return JSON.parse(value)
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export const authState = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  user: readUser()
})

export const getToken = () => authState.token

export const isAuthenticated = () => Boolean(authState.token)

export const setAuth = (loginResult) => {
  authState.token = loginResult.token
  authState.user = {
    userId: loginResult.userId,
    username: loginResult.username,
    nickname: loginResult.nickname
  }
  localStorage.setItem(TOKEN_KEY, authState.token)
  localStorage.setItem(USER_KEY, JSON.stringify(authState.user))
}

export const clearAuth = () => {
  authState.token = ''
  authState.user = null
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}
