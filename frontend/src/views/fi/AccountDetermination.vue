<template>
  <PageShell
    title="科目确定"
    tcode="OBYC"
  >
    <el-card>
      <el-table
        :data="rows"
        border
      >
        <el-table-column
          prop="accountKey"
          label="业务键"
        />
        <el-table-column
          label="科目"
          min-width="220"
        >
          <template #default="{ row }">
            <el-select
              v-model="row.saknr"
              filterable
              placeholder="选择总账科目"
            >
              <el-option
                v-for="account in accounts"
                :key="account.saknr"
                :label="`${account.saknr} ${account.txt}`"
                :value="account.saknr"
              />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <div class="footer-actions">
        <el-button
          type="primary"
          @click="save"
          >保存配置</el-button
        >
      </div>
    </el-card>
  </PageShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import { fiApi } from '../../api'

const rows = ref([])
const accounts = ref([])

async function load() {
  rows.value = await fiApi.determination()
  const result = await fiApi.gl({ page: 1, size: 100 })
  accounts.value = result.records || result || []
}

async function save() {
  await fiApi.updateDetermination(rows.value)
}

onMounted(load)
</script>
