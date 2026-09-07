<template><PageShell title="CO凭证" tcode="KSB1"><el-card><div class="toolbar"><el-input v-model="kostl" placeholder="成本中心" @keyup.enter="load" /><el-button @click="load">查询</el-button></div><el-table :data="rows" border stripe><el-table-column prop="belnr" label="CO凭证" /><el-table-column prop="kostl" label="成本中心" /><el-table-column prop="saknr" label="成本要素" /><el-table-column prop="amount" label="金额" /><el-table-column prop="text" label="文本" /></el-table></el-card></PageShell></template>
<script setup>
import { onMounted, ref } from 'vue'; import PageShell from '../../components/PageShell.vue'; import { coApi } from '../../api'; const rows=ref([]),kostl=ref('');async function load(){rows.value=await coApi.documents({kostl:kostl.value})};onMounted(load)
</script>
