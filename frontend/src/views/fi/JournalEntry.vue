<template>
  <PageShell
    title="总账凭证"
    tcode="FB50"
  >
    <el-card>
      <el-form
        :model="form"
        inline
      >
        <el-form-item label="凭证类型">
          <el-input v-model="form.blart" />
        </el-form-item>
        <el-form-item label="过账日期">
          <el-date-picker
            v-model="form.budat"
            type="date"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="抬头文本">
          <el-input v-model="form.text" />
        </el-form-item>
      </el-form>

      <el-table
        :data="form.items"
        border
      >
        <el-table-column
          label="科目"
          min-width="180"
        >
          <template #default="{ row }">
            <el-select
              v-model="row.saknr"
              filterable
              placeholder="选择科目"
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
        <el-table-column label="借/贷">
          <template #default="{ row }">
            <el-select v-model="row.shkzg">
              <el-option
                label="借方"
                value="S"
              />
              <el-option
                label="贷方"
                value="H"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="金额">
          <template #default="{ row }">
            <el-input-number
              v-model="row.amount"
              :min="0"
            />
          </template>
        </el-table-column>
        <el-table-column
          label="成本中心"
          min-width="180"
        >
          <template #default="{ row }">
            <el-select
              v-model="row.kostl"
              clearable
              filterable
              placeholder="选择成本中心"
            >
              <el-option
                v-for="center in centers"
                :key="center.kostl"
                :label="`${center.kostl} ${center.name}`"
                :value="center.kostl"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column
          label="供应商"
          min-width="180"
        >
          <template #default="{ row }">
            <el-select
              v-model="row.lifnr"
              clearable
              filterable
              placeholder="选择供应商"
            >
              <el-option
                v-for="vendor in vendors"
                :key="vendor.lifnr"
                :label="`${vendor.lifnr} ${vendor.name}`"
                :value="vendor.lifnr"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column
          label="客户"
          min-width="180"
        >
          <template #default="{ row }">
            <el-select
              v-model="row.kunnr"
              clearable
              filterable
              placeholder="选择客户"
            >
              <el-option
                v-for="customer in customers"
                :key="customer.kunnr"
                :label="`${customer.kunnr} ${customer.name}`"
                :value="customer.kunnr"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="文本">
          <template #default="{ row }">
            <el-input v-model="row.text" />
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ $index }">
            <el-button
              link
              @click="remove($index)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <el-button
        class="add-line"
        @click="add"
        >添加行</el-button
      >

      <div class="balance-footer">
        <span>借方 {{ debit.toFixed(2) }}</span>
        <span>贷方 {{ credit.toFixed(2) }}</span>
        <span :class="{ danger: difference }">差额 {{ difference.toFixed(2) }}</span>
        <el-button
          type="primary"
          :disabled="difference !== 0 || form.items.length < 2"
          @click="post"
        >
          过账
        </el-button>
      </div>
    </el-card>

    <el-dialog
      v-model="visible"
      title="过账成功"
    >
      <el-result
        icon="success"
        title="FI凭证已创建"
        :sub-title="result?.belnr"
      />
    </el-dialog>
  </PageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import { coApi } from '../../api/co'
import { fiApi } from '../../api'
import { mmApi } from '../../api/mm'
import { sdApi } from '../../api/sd'

const form = reactive({
  blart: 'SA',
  budat: '',
  text: '',
  items: [
    { saknr: '1001', shkzg: 'S', amount: 0 },
    { saknr: '4001', shkzg: 'H', amount: 0 },
  ],
})
const accounts = ref([])
const centers = ref([])
const vendors = ref([])
const customers = ref([])
const visible = ref(false)
const result = ref(null)

const debit = computed(() =>
  form.items.filter((item) => item.shkzg === 'S').reduce((sum, item) => sum + Number(item.amount || 0), 0),
)
const credit = computed(() =>
  form.items.filter((item) => item.shkzg === 'H').reduce((sum, item) => sum + Number(item.amount || 0), 0),
)
const difference = computed(() => Math.abs(debit.value - credit.value))

function add() {
  form.items.push({ saknr: '', shkzg: 'S', amount: 0 })
}

function remove(index) {
  form.items.splice(index, 1)
}

async function post() {
  result.value = await fiApi.post(form)
  visible.value = true
}

onMounted(async () => {
  const gl = await fiApi.gl({ page: 1, size: 100 })
  accounts.value = gl.records || gl || []
  centers.value = await coApi.centers()
  vendors.value = (await mmApi.vendors({ page: 1, size: 100 })).records || []
  customers.value = (await sdApi.customers({ page: 1, size: 100 })).records || []
})
</script>
