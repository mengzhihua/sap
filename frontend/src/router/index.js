import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/Layout.vue'
import { auth } from '../auth'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Page from '../views/Page.vue'

const groups = [
  { name: 'MM 物料管理', children: [
    ['MM01', '物料主数据', '/mm/materials'], ['MK01', '供应商主数据', '/mm/vendors'],
    ['ME51N', '采购申请', '/mm/pr'], ['ME21N', '采购订单', '/mm/po'],
    ['MIGO', '货物移动', '/mm/migo'], ['MMBE', '库存总览', '/mm/stock'],
    ['MIRO', '发票校验', '/mm/miro'], ['MB03', '物料凭证', '/mm/material-docs'],
    ['MIR4', '供应商发票', '/mm/invoices'], ['GRIR', 'GR/IR', '/mm/gr-ir']
  ]},
  { name: 'SD 销售管理', children: [
    ['XD01', '客户主数据', '/sd/customers'], ['VA01', '销售订单', '/sd/so'],
    ['VL01N', '交货单', '/sd/dn'], ['VF01', '开票', '/sd/billing']
  ]},
  { name: 'FI 财务会计', children: [
    ['FS00', '总账科目', '/fi/gl'], ['FB50', '总账凭证', '/fi/fb50'],
    ['FB03', '财务凭证', '/fi/documents'], ['F-53', '付款', '/fi/payments'],
    ['FBL1N', '应付未清项', '/fi/ap'], ['FBL5N', '应收未清项', '/fi/ar'],
    ['S_ALR', '余额报表', '/fi/balances'], ['OBYC', '科目确定', '/fi/determination']
  ]},
  { name: 'CO 管理会计', children: [
    ['KS01', '成本中心', '/co/cost-centers'], ['KSB1', 'CO 凭证', '/co/documents'], ['S_ALR_CO', 'CO 报表', '/co/report']
  ]},
  { name: 'PP 生产计划', children: [
    ['CS01', 'BOM', '/pp/bom'], ['CO01', '生产订单', '/pp/orders']
  ]},
  { name: 'Basis 基础管理', children: [
    ['SU01', '用户管理', '/basis/users'], ['OX10', '组织结构', '/basis/org'],
    ['SE93', '事务代码', '/basis/tcodes'], ['SM20', '操作日志', '/basis/op-logs']
  ]},
  { name: '集成 Integration', children: [
    ['SLG1', '集成日志', '/integration/logs'], ['INTF', '接口文档', '/integration/docs']
  ]}
]

const routes = [
  { path: '/login', component: Login },
  { path: '/', component: Layout, redirect: '/dashboard', children: [
    { path: 'dashboard', component: Dashboard, meta: { title: '工作台', tcode: 'SAP' } },
    ...groups.flatMap((g) => g.children.map(([tcode, title, path]) => ({
      path: path.substring(1), component: Page, meta: { title, tcode, endpoint: path }
    })))
  ]}
]
const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to) => {
  if (to.path === '/login') return auth.token ? '/dashboard' : true
  if (!auth.token) return { path: '/login', query: { redirect: to.fullPath } }
  return true
})
export { groups }
export default router
