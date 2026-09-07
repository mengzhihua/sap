<template><PageShell title="应收未清项" tcode="FBL5N"><el-card><el-table :data="rows" border stripe><el-table-column prop="belnr" label="凭证" /><el-table-column prop="kunnr" label="客户" /><el-table-column prop="amount" label="金额" /><el-table-column prop="budat" label="日期" /></el-table></el-card></PageShell></template>
<script setup>
import { onMounted, ref } from 'vue'; import PageShell from '../../components/PageShell.vue'; import { fiApi } from '../../api'; const rows=ref([]);onMounted(async()=>{rows.value=await fiApi.ar()})
</script>
