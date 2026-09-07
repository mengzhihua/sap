<template>
  <PageShell
    title="事务代码目录"
    tcode="SE16"
    ><el-card
      ><div class="toolbar">
        <el-input
          v-model="q"
          placeholder="事务代码/名称"
          @keyup.enter="load"
        /><el-button @click="load">查询</el-button>
      </div>
      <el-table
        :data="rows"
        border
        stripe
        ><el-table-column
          prop="tcode"
          label="事务代码" /><el-table-column
          prop="module"
          label="模块" /><el-table-column
          prop="name"
          label="名称" /><el-table-column
          prop="route"
          label="前端路由" /></el-table></el-card
  ></PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import { basisApi } from '../../api'
const q = ref('')
const rows = ref([])
async function load() {
  rows.value = await basisApi.tcodes(q.value)
}
onMounted(load)
</script>
