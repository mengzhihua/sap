<template>
  <PageShell
    title="接口文档"
    tcode="INTF"
    ><el-card
      ><h3>SRM → SAP 入站接口</h3>
      <p class="muted">
        认证：HTTP Basic Auth，用户名 <code>srm</code>，密码 <code>srm123</code>。成功响应统一为 OData 形状。
      </p>
      <el-table
        :data="srm"
        border
        ><el-table-column
          prop="method"
          label="方法"
          width="90" /><el-table-column
          prop="path"
          label="路径" /><el-table-column
          prop="fields"
          label="关键字段"
      /></el-table>
      <h3 class="section-title">BMS ↔ SAP 接口</h3>
      <el-table
        :data="bms"
        border
        ><el-table-column
          prop="direction"
          label="方向"
          width="100" /><el-table-column
          prop="method"
          label="方法"
          width="90" /><el-table-column
          prop="path"
          label="路径" /><el-table-column
          prop="fields"
          label="关键字段"
      /></el-table>
      <h3 class="section-title">认证与幂等</h3>
      <p>
        开放入站接口使用 <code>X-Api-Key: sap-open-key</code>；交货推送包含
        deliveryNo、warehouseCode、items；对账单入站包含 statementNo、gjahr、belnr、amount、currency。
      </p></el-card
    ></PageShell
  >
</template>
<script setup>
import PageShell from '../../components/PageShell.vue'
const srm = [
  {
    method: 'POST',
    path: '/API_PURCHASEORDER_PROCESS_SRV/A_PurchaseOrder',
    fields: 'PurchaseOrderType, Supplier, PurchasingOrganization, to_PurchaseOrderItem',
  },
  {
    method: 'POST',
    path: '/API_MATERIAL_DOCUMENT_SRV/A_MaterialDocumentHeader',
    fields: 'GoodsMovementCode, PostingDate, to_MaterialDocumentItem',
  },
  {
    method: 'POST',
    path: '/API_SUPPLIERINVOICE_PROCESS_SRV/A_SupplierInvoice',
    fields: 'Supplier, DocumentDate, GrossAmount, PurchaseOrder',
  },
  {
    method: 'POST',
    path: '/API_SUPPLIER_EVALUATION_PROCESS_SRV/A_SupplierEvaluation',
    fields: 'Supplier, Score, Grade, Comments',
  },
]
const bms = [
  {
    direction: '入站',
    method: 'POST',
    path: '/api/open/bms/statements',
    fields: 'statementNo, gjahr, belnr, amount, currency, lines',
  },
  { direction: '入站', method: 'GET', path: '/api/open/bms/statements/{statementNo}', fields: 'statementNo' },
  {
    direction: '出站',
    method: 'POST',
    path: '/api/bms/deliveries',
    fields: 'deliveryNo, warehouseCode, items',
  },
  { direction: '出站', method: 'POST', path: '/api/bms/receipts', fields: 'receiptNo, warehouseCode, items' },
]
</script>
