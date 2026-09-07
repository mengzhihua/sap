<template>
  <PageShell
    title="发票校验"
    tcode="MIRO"
    ><el-card
      ><el-form inline
        ><el-form-item label="采购订单"
          ><el-select
            v-model="selected"
            filterable
            @change="loadPo"
            ><el-option
              v-for="po in pos"
              :key="po.ebeln"
              :label="`${po.ebeln} ${po.lifnr}`"
              :value="po.ebeln" /></el-select></el-form-item
        ><el-form-item label="供应商"><el-input v-model="form.lifnr" /></el-form-item
        ><el-form-item label="税率"
          ><el-select v-model="tax"
            ><el-option
              v-for="x in [13, 9, 6, 0]"
              :key="x"
              :label="`${x}%`"
              :value="x" /></el-select></el-form-item></el-form
      ><el-table
        :data="form.items"
        border
        ><el-table-column
          prop="ebelp"
          label="行号"
        /><el-table-column
          prop="matnr"
          label="物料"
        /><el-table-column
          prop="remainingQty"
          label="GR-IR剩余"
        /><el-table-column label="本次数量"
          ><template #default="{ row }"
            ><el-input-number
              v-model="row.qty"
              :min="0"
              :max="row.remainingQty" /></template></el-table-column
        ><el-table-column label="发票单价"
          ><template #default="{ row }"
            ><el-input-number
              v-model="row.price"
              :min="0" /></template></el-table-column
        ><el-table-column label="含税金额">{{ gross.toFixed(2) }}</el-table-column></el-table
      >
      <div class="total-line">
        含税总额：<strong>{{ gross.toFixed(2) }}</strong>
      </div>
      <el-button
        type="primary"
        @click="submit"
        >模拟发票校验</el-button
      ></el-card
    ><el-card
      v-if="result"
      class="mt"
      ><el-tag :type="result.status === 'POSTED' ? 'success' : 'danger'">{{ result.status }}</el-tag
      ><span class="ml">{{ result.matchResult || result.match_result }}</span></el-card
    ></PageShell
  >
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import { mmApi } from '../../api'
const pos = ref([])
const selected = ref('')
const tax = ref(13)
const result = ref(null)
const form = reactive({ lifnr: '', ebeln: '', items: [] })
const gross = computed(
  () => form.items.reduce((a, x) => a + (x.qty || 0) * (x.price || 0), 0) * (1 + tax.value / 100),
)
async function loadPo() {
  const po = await mmApi.po(selected.value)
  form.ebeln = po.ebeln
  form.lifnr = po.lifnr
  form.items = (po.items || []).map((x) => ({
    ebeln: po.ebeln,
    ebelp: x.ebelp,
    matnr: x.matnr,
    remainingQty: Math.max(0, (x.deliveredQty || 0) - (x.invoicedQty || 0)),
    qty: Math.max(0, (x.deliveredQty || 0) - (x.invoicedQty || 0)),
    price: x.netpr || 0,
  }))
}
async function submit() {
  result.value = await mmApi.miro(form)
}
onMounted(async () => {
  pos.value = await mmApi.pos()
})
</script>
