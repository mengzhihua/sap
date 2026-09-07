import http from './request'

export const sdApi = {
  customers: (params) => http.get('/sd/customers', { params }),
  createCustomer: (data) => http.post('/sd/customers', data),
  updateCustomer: (id, data) => http.put(`/sd/customers/${id}`, data),
  sos: () => http.get('/sd/so'),
  so: (id) => http.get(`/sd/so/${id}`),
  createSo: (data) => http.post('/sd/so', data),
  dns: () => http.get('/sd/dn'),
  dn: (id) => http.get(`/sd/dn/${id}`),
  createDn: (data) => http.post('/sd/dn', data),
  pick: (id) => http.post(`/sd/dn/${id}/pick`),
  pgi: (id) => http.post(`/sd/dn/${id}/pgi`),
  billings: () => http.get('/sd/billing'),
  billing: (id) => http.get(`/sd/billing/${id}`),
  createBilling: (data) => http.post('/sd/billing', data),
  repush: (id) => http.post(`/integration/bms/deliveries/${id}/push`),
}
