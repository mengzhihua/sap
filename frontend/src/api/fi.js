import http from './request'

export const fiApi = {
  gl: (params) => http.get('/fi/gl-accounts', { params }),
  createGl: (data) => http.post('/fi/gl-accounts', data),
  updateGl: (id, data) => http.put(`/fi/gl-accounts/${id}`, data),
  documents: (params) => http.get('/fi/documents', { params }),
  document: (id) => http.get(`/fi/documents/${id}`),
  post: (data) => http.post('/fi/documents', data),
  reverse: (id) => http.post(`/fi/documents/${id}/reverse`),
  payments: () => http.get('/fi/payments'),
  pay: (data) => http.post('/fi/payments', data),
  balances: () => http.get('/fi/balances'),
  ap: (params) => http.get('/fi/ap/open-items', { params }),
  ar: (params) => http.get('/fi/ar/open-items', { params }),
  determination: () => http.get('/fi/account-determination'),
  updateDetermination: (data) => http.put('/fi/account-determination', data),
}
