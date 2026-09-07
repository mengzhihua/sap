<template>
  <PageShell
    title="物料凭证"
    tcode="MB51"
  >
    <el-card>
      <div class="toolbar">
        <el-input
          v-model="q"
          placeholder="物料凭证号"
        />
        <el-button @click="load">查询</el-button>
      </div>
      <el-table
        :data="rows"
        border
        stripe
      >
        <el-table-column
          prop="mblnr"
          label="物料凭证"
        />
        <el-table-column
          prop="mjahr"
          label="年度"
        />
        <el-table-column
          prop="bwart"
          label="移动类型"
        />
        <el-table-column
          prop="fiBelnr"
          label="FI凭证"
        >
          <template #default="{ row }">
            <el-link @click="show(row)">{{ row.fiBelnr || '-' }}</el-link>
          </template>
        </el-table-column>
        <el-table-column
          prop="createdAt"
          label="创建时间"
        />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button
              link
              @click="show(row)"
              >明细</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-drawer
      v-model="visible"
      title="物料凭证明细"
      size="60%"
    >
      <el-descriptions
        v-if="selected"
        :column="3"
        border
      >
        <el-descriptions-item label="物料凭证">{{ selected.mblnr }}</el-descriptions-item>
        <el-descriptions-item label="年度">{{ selected.mjahr }}</el-descriptions-item>
        <el-descriptions-item label="移动类型">{{ selected.bwart }}</el-descriptions-item>
        <el-descriptions-item label="FI凭证">{{ selected.fiBelnr || '-' }}</el-descriptions-item>
        <el-descriptions-item label="参考单据">{{ selected.refNo || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table
        v-if="selected"
        :data="selected.items || []"
        border
        class="drawer-table"
      >
        <el-table-column
          prop="mblnr"
          label="物料凭证"
        />
        <el-table-column
          prop="matnr"
          label="物料"
        />
        <el-table-column
          prop="werks"
          label="工厂"
        />
        <el-table-column
          prop="lgort"
          label="库存地点"
        />
        <el-table-column
          prop="bwart"
          label="移动类型"
        />
        <el-table-column
          prop="menge"
          label="数量"
        />
      </el-table>
    </el-drawer>
  </PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import { mmApi } from '../../api'
const q = ref('')
const rows = ref([])
const visible = ref(false)
const selected = ref(null)
async function load() {
  rows.value = await mmApi.materialDocs({ q: q.value, page: 1, size: 50 })
}
async function show(r) {
  selected.value = await mmApi.materialDoc(r.mblnr)
  visible.value = true
}
onMounted(load)
</script>
