<template>
  <PageShell
    title="余额报表"
    tcode="FAGLB03"
    ><el-card
      ><el-table
        :data="rows"
        border
        stripe
        ><el-table-column
          prop="saknr"
          label="科目" /><el-table-column
          prop="txt"
          label="名称" /><el-table-column
          prop="debit"
          label="借方" /><el-table-column
          prop="credit"
          label="贷方" /><el-table-column
          prop="balance"
          label="余额" /></el-table></el-card
  ></PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import { fiApi } from '../../api'
const rows = ref([])
onMounted(async () => {
  rows.value = await fiApi.balances()
})
</script>
