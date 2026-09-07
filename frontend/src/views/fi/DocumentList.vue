<template>
  <PageShell
    title="财务凭证"
    tcode="FB03"
  >
    <el-card>
      <div class="toolbar">
        <el-select
          v-model="filters.blart"
          clearable
          placeholder="凭证类型"
        >
          <el-option
            label="SA"
            value="SA"
          />
          <el-option
            label="KZ"
            value="KZ"
          />
          <el-option
            label="DZ"
            value="DZ"
          />
        </el-select>
        <el-date-picker
          v-model="filters.date"
          type="date"
          value-format="YYYY-MM-DD"
        />
        <el-button @click="load">查询</el-button>
      </div>

      <el-table
        :data="rows"
        border
        stripe
      >
        <el-table-column
          prop="belnr"
          label="凭证号"
        />
        <el-table-column
          prop="blart"
          label="类型"
        />
        <el-table-column
          prop="budat"
          label="过账日期"
        />
        <el-table-column
          prop="headerText"
          label="摘要"
        />
        <el-table-column
          prop="reversedBy"
          label="冲销凭证"
        />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button
              link
              @click="show(row)"
              >明细</el-button
            >
            <el-button
              v-if="!row.reversedBy && row.blart !== 'AB'"
              link
              type="danger"
              @click="reverse(row)"
            >
              冲销
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer
      v-model="visible"
      title="财务凭证明细"
      size="65%"
    >
      <el-descriptions
        v-if="selected"
        :column="3"
        border
      >
        <el-descriptions-item label="凭证号">{{ selected.belnr }}</el-descriptions-item>
        <el-descriptions-item label="凭证类型">{{ selected.blart }}</el-descriptions-item>
        <el-descriptions-item label="过账日期">{{ selected.budat }}</el-descriptions-item>
        <el-descriptions-item label="摘要">{{ selected.headerText }}</el-descriptions-item>
        <el-descriptions-item label="冲销凭证">{{ selected.reversedBy || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table
        v-if="selected"
        :data="selected.items || []"
        border
        class="drawer-table"
      >
        <el-table-column
          prop="buzei"
          label="行号"
        />
        <el-table-column
          prop="saknr"
          label="科目"
        />
        <el-table-column
          prop="shkzg"
          label="借贷"
        />
        <el-table-column
          prop="amount"
          label="金额"
        />
        <el-table-column
          prop="kostl"
          label="成本中心"
        />
        <el-table-column
          prop="lifnr"
          label="供应商"
        />
        <el-table-column
          prop="kunnr"
          label="客户"
        />
        <el-table-column
          prop="text"
          label="文本"
        />
      </el-table>
    </el-drawer>
  </PageShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import PageShell from '../../components/PageShell.vue'
import { fiApi } from '../../api'

const rows = ref([])
const filters = reactive({ blart: '', date: '' })
const visible = ref(false)
const selected = ref(null)

async function load() {
  const params = {}
  if (filters.blart) params.blart = filters.blart
  const result = await fiApi.documents(params)
  rows.value = result.records || result || []
}

async function show(row) {
  selected.value = await fiApi.document(row.belnr)
  visible.value = true
}

async function reverse(row) {
  await ElMessageBox.confirm('确认冲销该凭证？', '提示')
  await fiApi.reverse(row.belnr)
  await load()
}

onMounted(load)
</script>
