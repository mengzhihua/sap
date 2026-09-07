import http from './request'

export const basisApi = {
  users: () => http.get('/basis/users'),
  createUser: (data) => http.post('/basis/users', data),
  updateUser: (id, data) => http.put(`/basis/users/${id}`, data),
  resetPassword: (id, data) => http.put(`/basis/users/${id}/password`, data),
  removeUser: (id) => http.delete(`/basis/users/${id}`),
  org: (kind) => http.get(`/basis/org/${kind}`),
  createOrg: (kind, data) => http.post(`/basis/org/${kind}`, data),
  tcodes: (q) => http.get('/basis/tcodes', { params: { q } }),
  logs: (params) => http.get('/basis/op-logs', { params }),
}
