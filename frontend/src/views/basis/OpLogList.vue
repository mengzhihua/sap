<template>
  <PageShell
    title="操作日志"
    tcode="SM37"
    ><el-card
      ><el-table
        :data="rows"
        border
        stripe
        ><el-table-column
          prop="createdAt"
          label="时间" /><el-table-column
          prop="username"
          label="用户" /><el-table-column
          prop="method"
          label="方法" /><el-table-column
          prop="path"
          label="路径" /><el-table-column
          prop="status"
          label="结果" /></el-table
      ><TablePager
        v-bind="query"
        :total="total"
        @change="change" /></el-card
  ></PageShell>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import TablePager from '../../components/TablePager.vue'
import { basisApi } from '../../api'
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20 })
async function load() {
  const r = await basisApi.logs(query)
  rows.value = r.records || []
  total.value = r.total || 0
}
function change(v) {
  query.page = v
  load()
}
onMounted(load)
</script>
