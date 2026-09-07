<template>
  <PageShell
    title="成本中心报表"
    tcode="S_ALR_87013611"
    ><el-card
      ><el-table
        :data="pivot"
        border
        ><el-table-column
          prop="kostl"
          label="成本中心"
          fixed /><el-table-column
          v-for="key in elements"
          :key="key"
          :prop="key"
          :label="key" /><el-table-column
          prop="total"
          label="合计" /></el-table></el-card
  ></PageShell>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import { coApi } from '../../api'
const rows = ref([])
const elements = computed(() => [...new Set(rows.value.map((x) => x.saknr || x.costElement).filter(Boolean))])
const pivot = computed(() => {
  const map = {}
  rows.value.forEach((x) => {
    const k = x.kostl || x.costCenter
    map[k] ||= { kostl: k, total: 0 }
    const e = x.saknr || x.costElement
    map[k][e] = (map[k][e] || 0) + Number(x.amount || 0)
    map[k].total += Number(x.amount || 0)
  })
  return Object.values(map)
})
onMounted(async () => {
  rows.value = await coApi.report()
})
</script>
