<template>
  <div>
    <h2>Launchpad 工作台</h2>
    <el-row :gutter="16" class="stats">
      <el-col v-for="c in cards" :key="c.key" :span="4"><el-card><div class="muted">{{ c.label }}</div><strong>{{ data[c.key] ?? 0 }}</strong></el-card></el-col>
    </el-row>
    <h3>模块导航</h3>
    <div v-for="group in groups" :key="group.name" class="tile-group"><h3>{{ group.name }}</h3><el-row :gutter="14">
      <el-col v-for="item in group.children" :key="item[2]" :xs="12" :sm="8" :md="6" :lg="4"><el-card class="tile" shadow="hover" @click="$router.push(item[2])"><b>{{ item[0] }}</b><span>{{ item[1] }}</span></el-card></el-col>
    </el-row></div>
  </div>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { dashboardApi } from '../api'
import { groups } from '../router'
const data = ref({})
const cards = computed(() => [
  { key: 'poCount', label: '采购订单' }, { key: 'poOpenCount', label: '开放订单' },
  { key: 'stockValue', label: '库存金额' }, { key: 'monthRevenue', label: '本月收入' },
  { key: 'monthCost', label: '本月成本' }, { key: 'integrationSuccessRate', label: '接口成功率' }
])
onMounted(async () => { data.value = await dashboardApi.summary() })
</script>
