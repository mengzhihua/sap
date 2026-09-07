<template>
  <PageShell
    title="经营驾驶舱"
    tcode="SAP"
  >
    <el-row
      :gutter="16"
      class="dashboard-stats"
    >
      <el-col
        v-for="card in cards"
        :key="card.key"
        :xs="12"
        :sm="8"
        :md="4"
      >
        <el-card
          ><div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ display(card) }}</div></el-card
        >
      </el-col>
    </el-row>
    <el-row
      :gutter="16"
      class="dashboard-row"
    >
      <el-col
        :md="10"
        :sm="24"
        ><el-card
          ><template #header>生产订单状态</template>
          <div
            v-for="item in prodRows"
            :key="item.status"
            class="status-row"
          >
            <span>{{ item.status }}</span
            ><el-progress
              :percentage="item.percent"
              :format="() => `${item.count}`"
            /></div></el-card
      ></el-col>
      <el-col
        :md="14"
        :sm="24"
        ><el-card
          ><template #header>最近财务凭证</template
          ><el-table
            :data="data.recentDocuments || []"
            size="small"
            ><el-table-column
              prop="belnr"
              label="凭证号" /><el-table-column
              prop="blart"
              label="类型"
              width="80" /><el-table-column
              prop="budat"
              label="过账日期" /><el-table-column
              prop="headerText"
              label="摘要"
              show-overflow-tooltip /></el-table></el-card
      ></el-col>
    </el-row>
  </PageShell>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import PageShell from '../components/PageShell.vue'
import { dashboardApi } from '../api'
const data = ref({})
const cards = [
  { key: 'poCount', label: '采购订单' },
  { key: 'poOpenCount', label: '开放订单' },
  { key: 'stockValue', label: '库存金额' },
  { key: 'monthRevenue', label: '本月收入' },
  { key: 'monthCost', label: '本月成本' },
  { key: 'apOpen', label: '应付未清' },
  { key: 'arOpen', label: '应收未清' },
  { key: 'integrationSuccessRate', label: '接口成功率' },
]
const prodRows = computed(() => {
  const source = data.value.prodOrderStatus || {}
  const max = Math.max(1, ...Object.values(source))
  return ['CRTD', 'REL', 'CNF', 'TECO'].map((status) => ({
    status,
    count: source[status] || 0,
    percent: Math.round(((source[status] || 0) / max) * 100),
  }))
})
function display(card) {
  return card.key === 'integrationSuccessRate'
    ? `${((data.value[card.key] || 0) * 100).toFixed(1)}%`
    : Number(data.value[card.key] || 0).toLocaleString()
}
onMounted(async () => {
  data.value = await dashboardApi.summary()
})
</script>
