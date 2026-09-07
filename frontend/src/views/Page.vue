<template>
  <div>
    <div class="page-head"><h2>{{ $route.meta.tcode }} – {{ $route.meta.title }}</h2><el-button type="primary" @click="load">刷新</el-button></div>
    <el-card v-if="$route.meta.endpoint === '/integration/docs'">
      <h3>SRM 入站接口</h3><p>Basic Auth：srm/srm123</p><el-table :data="srm"><el-table-column prop="path" label="路径" /><el-table-column prop="method" label="方法" /><el-table-column prop="payload" label="核心字段" /></el-table>
      <h3>BMS 接口</h3><p>X-Api-Key：sap-open-key；入站 /api/open/bms/statements，出站 OMS/WMS docs。</p>
    </el-card>
    <el-card v-else>
      <div class="toolbar"><el-input v-model="query" placeholder="搜索关键字" clearable @keyup.enter="load" /><el-button @click="load">查询</el-button>
        <el-button v-if="action" type="primary" @click="submitAction">{{ action }}</el-button></div>
      <el-table :data="rows" stripe border height="560" @row-dblclick="detail">
        <el-table-column v-for="key in columns" :key="key" :prop="key" :label="labels[key] || key" min-width="140" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="loaded && !rows.length" description="暂无数据" />
    </el-card>
    <el-dialog v-model="drawer" title="详情" width="70%"><pre>{{ JSON.stringify(selected, null, 2) }}</pre></el-dialog>
  </div>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../api'
import { ElMessage } from 'element-plus'
const route = useRoute(); const rows = ref([]); const selected = ref({}); const drawer = ref(false); const loaded = ref(false); const query = ref('')
const labels = { matnr: '物料', maktx: '名称', lifnr: '供应商', name: '名称', status: '状态', ebeln: '采购订单', banfn: '采购申请', vbeln: '单据号', belnr: '凭证号', kunnr: '客户', amount: '金额', totalAmount: '总金额', createdAt: '创建时间', direction: '方向', systemName: '系统', actionName: '动作', request: '请求', response: '响应' }
const srm = [{ path: '/API_PURCHASEORDER_PROCESS_SRV/A_PurchaseOrder', method: 'POST', payload: 'Supplier, to_PurchaseOrderItem' }, { path: '/API_MATERIAL_DOCUMENT_SRV/A_MaterialDocumentHeader', method: 'POST', payload: 'GoodsMovementCode, to_MaterialDocumentItem' }, { path: '/API_SUPPLIERINVOICE_PROCESS_SRV/A_SupplierInvoice', method: 'POST', payload: 'Supplier, PurchaseOrder' }, { path: '/API_SUPPLIER_EVALUATION_PROCESS_SRV/A_SupplierEvaluation', method: 'POST', payload: 'Supplier, Score, Grade' }]
const action = computed(() => route.path === '/sd/dn' ? '重载交货单' : route.path === '/pp/orders' ? '刷新订单' : '')
const methods = {
  '/mm/materials': () => api.materials({ q: query.value, page: 1, size: 100 }), '/mm/vendors': () => api.vendors({ q: query.value, page: 1, size: 100 }),
  '/mm/pr': api.prs, '/mm/po': api.pos, '/mm/migo': api.materialDocs, '/mm/stock': () => api.stock({ matnr: query.value }),
  '/mm/material-docs': api.materialDocs, '/mm/invoices': api.invoices, '/mm/gr-ir': api.grir, '/mm/miro': api.invoices, '/mm/evaluations': api.evaluations,
  '/sd/customers': api.customers, '/sd/so': api.sos, '/sd/dn': api.dns, '/sd/billing': api.billings,
  '/fi/gl': () => api.gl({ q: query.value, page: 1, size: 100 }), '/fi/fb50': api.fiDocs, '/fi/documents': () => api.fiDocs({ page: 1, size: 100 }),
  '/fi/payments': api.payments, '/fi/ap': api.ap, '/fi/ar': api.ar, '/fi/balances': api.balances, '/fi/determination': api.determination,
  '/co/cost-centers': api.costCenters, '/co/documents': api.coDocs, '/co/report': api.coReport, '/pp/bom': api.boms, '/pp/orders': api.orders,
  '/basis/users': api.users, '/basis/org': () => api.org('plants'), '/basis/tcodes': () => api.tcodesList(query.value), '/basis/op-logs': () => api.opLogs({ page: 1, size: 100 }),
  '/integration/logs': () => api.integrationLogs({ page: 1, size: 100 })
}
const columns = computed(() => rows.value.length ? Object.keys(rows.value[0]).filter((x) => !['items', 'components', 'password'].includes(x)).slice(0, 9) : ['status'])
async function load() {
  if (route.meta.endpoint === '/integration/docs') return
  const fn = methods[route.meta.endpoint]; if (!fn) return
  const data = await fn(); rows.value = data?.records || data || []; loaded.value = true
}
function detail(row) { selected.value = row; drawer.value = true }
async function submitAction() { await load(); ElMessage.success('已刷新') }
onMounted(load)
</script>
