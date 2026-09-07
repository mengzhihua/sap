import http from './request'

export const coApi = {
  centers: () => http.get('/co/cost-centers'),
  createCenter: (data) => http.post('/co/cost-centers', data),
  updateCenter: (id, data) => http.put(`/co/cost-centers/${id}`, data),
  documents: (params) => http.get('/co/documents', { params }),
  report: () => http.get('/co/report')
}
