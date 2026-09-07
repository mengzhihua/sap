import { reactive } from 'vue'

const TOKEN_KEY = 'sap_token'
const USER_KEY = 'sap_user'
export const auth = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  user: JSON.parse(localStorage.getItem(USER_KEY) || 'null'),
})
export function setAuth(token, user) {
  auth.token = token
  auth.user = user
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}
export function clearAuth() {
  auth.token = ''
  auth.user = null
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}
