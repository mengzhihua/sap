<template>
  <PageShell
    title="创建采购订单"
    tcode="ME21N"
    ><template #actions><el-button @click="$router.push('/mm/po')">返回订单列表</el-button></template>
    <el-card
      ><el-form
        :model="form"
        inline
        label-width="90px"
        ><el-form-item label="供应商"
          ><el-select
            v-model="form.supplierCode"
            filterable
            remote
            :remote-method="searchVendor"
            placeholder="选择供应商"
            ><el-option
              v-for="v in vendors"
              :key="v.lifnr"
              :label="`${v.lifnr} ${v.name}`"
              :value="v.lifnr" /></el-select></el-form-item
        ><el-form-item label="采购组织"><el-input v-model="form.ekorg" /></el-form-item
        ><el-form-item label="采购组"><el-input v-model="form.ekgrp" /></el-form-item
        ><el-form-item label="公司代码"><el-input v-model="form.bukrs" /></el-form-item
        ><el-form-item label="货币"><el-input v-model="form.waers" /></el-form-item
        ><el-form-item label="PR转单"
          ><el-input
            v-model="form.prBanfn"
            placeholder="可选" /></el-form-item></el-form
    ></el-card>
    <el-card class="mt"
      ><template #header
        ><span>采购订单项目</span
        ><el-button
          class="float-right"
          type="primary"
          link
          @click="add"
          >添加项目</el-button
        ></template
      ><el-table
        :data="form.items"
        border
        ><el-table-column
          label="物料"
          min-width="200"
          ><template #default="{ row }"
            ><el-select
              v-model="row.matnr"
              filterable
              remote
              :remote-method="searchMaterial"
              @change="fillPrice(row)"
              ><el-option
                v-for="m in materials"
                :key="m.matnr"
                :label="`${m.matnr} ${m.maktx}`"
                :value="m.matnr" /></el-select></template></el-table-column
        ><el-table-column label="工厂"
          ><template #default="{ row }"
            ><el-select v-model="row.werks"
              ><el-option
                v-for="p in plants"
                :key="p.werks"
                :label="p.werks"
                :value="p.werks" /></el-select></template></el-table-column
        ><el-table-column label="库存地点"
          ><template #default="{ row }"
            ><el-select v-model="row.lgort"
              ><el-option
                v-for="s in slocs.filter((x) => x.werks === row.werks)"
                :key="s.lgort"
                :label="s.lgort"
                :value="s.lgort" /></el-select></template></el-table-column
        ><el-table-column label="数量"
          ><template #default="{ row }"
            ><el-input-number
              v-model="row.qty"
              :min="0.001" /></template></el-table-column
        ><el-table-column label="单价"
          ><template #default="{ row }"
            ><el-input-number
              v-model="row.price"
              :min="0" /></template></el-table-column
        ><el-table-column label="交货日期"
          ><template #default="{ row }"
            ><el-date-picker
              v-model="row.deliveryDate"
              type="date"
              value-format="YYYY-MM-DD" /></template></el-table-column
        ><el-table-column label="行金额"
          ><template #default="{ row }">{{ money(row.qty * row.price) }}</template></el-table-column
        ><el-table-column label="操作"
          ><template #default="{ $index }"
            ><el-button
              link
              @click="form.items.splice($index, 1)"
              >删除</el-button
            ></template
          ></el-table-column
        ></el-table
      >
      <div class="total-line">
        订单总额：<strong>{{ money(total) }}</strong>
      </div></el-card
    >
    <div class="footer-actions">
      <el-button
        type="primary"
        size="large"
        :disabled="!form.items.length"
        @click="submit"
        >提交采购订单</el-button
      >
    </div>
    <el-dialog
      v-model="resultVisible"
      title="采购订单已创建"
      ><el-result
        icon="success"
        title="创建成功"
        :sub-title="result?.ebeln"
        ><template #extra
          ><el-button
            type="primary"
            @click="$router.push('/mm/po')"
            >查看订单</el-button
          ></template
        ></el-result
      ></el-dialog
    >
  </PageShell>
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import { mmApi } from '../../api'
import { basisApi } from '../../api/basis'
const form = reactive({
  supplierCode: '100010',
  ekorg: '1000',
  ekgrp: '001',
  bukrs: '1000',
  waers: 'CNY',
  items: [],
})
const vendors = ref([])
const materials = ref([])
const plants = ref([])
const slocs = ref([])
const result = ref(null)
const resultVisible = ref(false)
const total = computed(() =>
  form.items.reduce((a, x) => a + (Number(x.qty) || 0) * (Number(x.price) || 0), 0),
)
const money = (x) => Number(x || 0).toFixed(2)
function add() {
  form.items.push({ matnr: 'M1001', werks: '1000', lgort: '0001', qty: 1, price: 45, deliveryDate: '' })
}
async function searchVendor(q) {
  const r = await mmApi.vendors({ q, page: 1, size: 30 })
  vendors.value = r.records || []
}
async function searchMaterial(q) {
  const r = await mmApi.materials({ q, page: 1, size: 30 })
  materials.value = r.records || []
}
function fillPrice(row) {
  const m = materials.value.find((x) => x.matnr === row.matnr)
  if (m) row.price = m.stdPrice
}
async function submit() {
  result.value = await mmApi.createPo(form)
  resultVisible.value = true
}
onMounted(async () => {
  add()
  vendors.value = (await mmApi.vendors({ page: 1, size: 30 })).records || []
  materials.value = (await mmApi.materials({ page: 1, size: 30 })).records || []
  plants.value = await basisApi.org('plants')
  slocs.value = await basisApi.org('storage-locations')
})
</script>
