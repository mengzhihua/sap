<template><PageShell title="科目确定" tcode="OBYC"><el-card><el-table :data="rows" border><el-table-column prop="accountKey" label="业务键" /><el-table-column label="科目"><template #default="{row}"><el-input v-model="row.saknr" /></template></el-table-column></el-table><div class="footer-actions"><el-button type="primary" @click="save">保存配置</el-button></div></el-card></PageShell></template>
<script setup>
import { onMounted, ref } from 'vue'; import PageShell from '../../components/PageShell.vue'; import { fiApi } from '../../api'; const rows=ref([]);async function load(){rows.value=await fiApi.determination()};async function save(){await fiApi.updateDetermination(rows.value)};onMounted(load)
</script>
