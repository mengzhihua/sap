<template>
  <PageShell
    title="供应商评价"
    tcode="ME63"
    ><el-card
      ><el-table
        :data="rows"
        border
        stripe
        ><el-table-column
          prop="evaluationId"
          label="评价编号" /><el-table-column
          prop="lifnr"
          label="供应商" /><el-table-column
          prop="score"
          label="评分" /><el-table-column
          prop="grade"
          label="等级" /><el-table-column
          prop="createdAt"
          label="评价时间" /></el-table></el-card
  ></PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import { mmApi } from '../../api'
const rows = ref([])
onMounted(async () => {
  rows.value = await mmApi.evaluations()
})
</script>
