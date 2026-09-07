import http from './request'

export const ppApi = {
  boms: () => http.get('/pp/bom'),
  bom: (id, werks) => http.get(`/pp/bom/${id}`, { params: { werks } }),
  createBom: (data) => http.post('/pp/bom', data),
  orders: () => http.get('/pp/orders'),
  order: (id) => http.get(`/pp/orders/${id}`),
  createOrder: (data) => http.post('/pp/orders', data),
  action: (id, action, params) => http.post(`/pp/orders/${id}/${action}`, null, { params }),
}
