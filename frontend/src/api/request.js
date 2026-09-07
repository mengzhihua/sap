import axios from 'axios'
import { ElMessage } from 'element-plus'
import { auth, clearAuth } from '../auth'

const http = axios.create({ baseURL: '/api', timeout: 15000 })
http.interceptors.request.use((config) => {
  if (auth.token) config.headers.Authorization = `Bearer ${auth.token}`
  return config
})
http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && body.code !== undefined && body.code !== 0) {
      ElMessage.error(body.msg || '请求失败')
      return Promise.reject(new Error(body.msg || '请求失败'))
    }
    return body ? body.data : body
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuth()
      ElMessage.warning('登录已过期')
    } else ElMessage.error(error.response?.data?.msg || error.message || '网络错误')
    return Promise.reject(error)
  },
)
export default http
