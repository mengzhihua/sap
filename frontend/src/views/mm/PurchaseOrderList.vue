<template>
  <PageShell
    title="采购订单"
    tcode="ME21N"
    ><template #actions
      ><el-button
        type="primary"
        @click="$router.push('/mm/po/create')"
        >创建采购订单</el-button
      ></template
    >
    <el-card
      ><el-table
        :data="rows"
        border
        stripe
        ><el-table-column
          prop="ebeln"
          label="采购订单"
        /><el-table-column
          prop="lifnr"
          label="供应商"
        /><el-table-column
          prop="ekorg"
          label="采购组织"
        /><el-table-column
          prop="totalAmount"
          label="总金额"
        /><el-table-column label="状态"
          ><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column
        ><el-table-column
          label="操作"
          width="170"
          ><template #default="{ row }"
            ><el-button
              link
              @click="show(row)"
              >明细</el-button
            ><el-button
              link
              :disabled="row.status === 'CLOSED'"
              @click="close(row)"
              >关闭</el-button
            ></template
          ></el-table-column
        ></el-table
      ></el-card
    >
    <el-drawer
      v-model="visible"
      title="采购订单明细"
      size="68%"
      ><div v-if="selected">
        <el-descriptions
          :column="3"
          border
          ><el-descriptions-item label="订单">{{ selected.ebeln }}</el-descriptions-item
          ><el-descriptions-item label="供应商">{{ selected.lifnr }}</el-descriptions-item
          ><el-descriptions-item label="状态">{{ selected.status }}</el-descriptions-item></el-descriptions
        ><el-table
          :data="selected.items || []"
          border
          class="drawer-table"
          ><el-table-column
            prop="ebelp"
            label="行号" /><el-table-column
            prop="matnr"
            label="物料" /><el-table-column
            prop="menge"
            label="订单数量" /><el-table-column
            prop="deliveredQty"
            label="已收货" /><el-table-column
            prop="invoicedQty"
            label="已开票" /><el-table-column
            prop="netpr"
            label="单价"
        /></el-table></div
    ></el-drawer>
  </PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import { mmApi } from '../../api'
import { ElMessageBox } from 'element-plus'
const rows = ref([])
const visible = ref(false)
const selected = ref(null)
async function load() {
  rows.value = await mmApi.pos()
}
async function show(row) {
  selected.value = await mmApi.po(row.ebeln)
  visible.value = true
}
async function close(row) {
  await ElMessageBox.confirm('确认关闭该采购订单？', '提示')
  await mmApi.closePo(row.ebeln)
  load()
}
onMounted(load)
</script>
