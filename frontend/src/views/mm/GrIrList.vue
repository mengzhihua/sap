<template><PageShell title="GR/IR余额" tcode="GRIR"><el-card><el-table :data="rows" border stripe><el-table-column prop="ebeln" label="采购订单" /><el-table-column prop="ebelp" label="行号" /><el-table-column prop="grQty" label="收货数量" /><el-table-column prop="irQty" label="发票数量" /><el-table-column prop="remainingQty" label="待开票数量" /><el-table-column prop="amount" label="金额" /></el-table></el-card></PageShell></template>
<script setup>
import { onMounted, ref } from 'vue'; import PageShell from '../../components/PageShell.vue'; import { mmApi } from '../../api'; const rows=ref([]);onMounted(async()=>{rows.value=await mmApi.grir()})
</script>
