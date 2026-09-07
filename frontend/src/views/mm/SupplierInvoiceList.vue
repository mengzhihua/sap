<template>
  <PageShell
    title="供应商发票"
    tcode="MIR4"
  >
    <el-card>
      <el-table
        :data="rows"
        border
        stripe
      >
        <el-table-column
          prop="belnr"
          label="发票凭证"
        />
        <el-table-column
          prop="lifnr"
          label="供应商"
        />
        <el-table-column
          prop="ebeln"
          label="采购订单"
        />
        <el-table-column
          prop="status"
          label="状态"
        >
          <template #default="{ row }">
            <StatusTag :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column
          prop="grossAmount"
          label="含税金额"
        />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button
              link
              @click="show(row)"
              >查看明细</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-drawer
      v-model="visible"
      title="供应商发票明细"
      size="60%"
    >
      <el-descriptions
        v-if="selected"
        :column="3"
        border
      >
        <el-descriptions-item label="发票凭证">{{ selected.belnr }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ selected.lifnr }}</el-descriptions-item>
        <el-descriptions-item label="采购订单">{{ selected.ebeln }}</el-descriptions-item>
        <el-descriptions-item label="含税金额">{{ selected.grossAmount }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ selected.status }}</el-descriptions-item>
      </el-descriptions>
      <el-table
        v-if="selected"
        :data="selected.items || []"
        border
        class="drawer-table"
      >
        <el-table-column
          prop="ebelp"
          label="行号"
        />
        <el-table-column
          prop="matnr"
          label="物料"
        />
        <el-table-column
          prop="qty"
          label="数量"
        />
        <el-table-column
          prop="price"
          label="单价"
        />
        <el-table-column
          prop="amount"
          label="金额"
        />
      </el-table>
    </el-drawer>
  </PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import { mmApi } from '../../api'
const rows = ref([])
const visible = ref(false)
const selected = ref(null)
async function load() {
  rows.value = await mmApi.invoices()
}
async function show(r) {
  selected.value = await mmApi.invoice(r.belnr)
  visible.value = true
}
onMounted(load)
</script>
