import http from './request'

export const integrationApi = {
  logs: (params) => http.get('/integration/logs', { params }),
  log: (id) => http.get(`/integration/logs/${id}`)
}
