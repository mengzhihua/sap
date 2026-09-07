import http from './request'

export const authApi = { login: (data) => http.post('/auth/login', data) }
export const dashboardApi = { summary: () => http.get('/dashboard/summary') }
export { mmApi } from './mm'
export { sdApi } from './sd'
export { fiApi } from './fi'
export { coApi } from './co'
export { ppApi } from './pp'
export { basisApi } from './basis'
export { integrationApi } from './integration'
