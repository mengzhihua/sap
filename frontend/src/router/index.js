import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/Layout.vue'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Launchpad from '../views/Launchpad.vue'
import { auth } from '../auth'

import { groups } from './groups'

const routeRecords = groups.flatMap((group) =>
  group.children.map(([tcode, title, path, component]) => ({
    path: path.substring(1),
    component,
    meta: { tcode, title },
  })),
)
const routes = [
  { path: '/login', component: Login },
  {
    path: '/',
    component: Layout,
    redirect: '/launchpad',
    children: [
      { path: 'launchpad', component: Launchpad, meta: { tcode: 'SAP', title: 'Launchpad' } },
      { path: 'dashboard', component: Dashboard, meta: { tcode: 'SAP', title: '经营驾驶舱' } },
      {
        path: 'mm/po/create',
        component: () => import('../views/mm/PurchaseOrderCreate.vue'),
        meta: { tcode: 'ME21N', title: '创建采购订单' },
      },
      ...routeRecords,
    ],
  },
]
const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to) => {
  if (to.path === '/login') return auth.token ? '/launchpad' : true
  if (!auth.token) return { path: '/login', query: { redirect: to.fullPath } }
  return true
})
export default router
