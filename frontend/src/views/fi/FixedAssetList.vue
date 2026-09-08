<template>
  <PageShell
    title="固定资产会计"
    tcode="FI-AA"
  >
    <template #actions>
      <el-button @click="depreciationVisible = true">AFAB 折旧运行</el-button>
      <el-button
        type="primary"
        @click="createVisible = true"
        >AS01 创建资产</el-button
      >
    </template>

    <div class="asset-summary">
      <div>
        <span>资产数量</span><strong>{{ rows.length }}</strong>
      </div>
      <div>
        <span>资产原值</span><strong>¥ {{ money(totals.acquisition) }}</strong>
      </div>
      <div>
        <span>累计折旧</span><strong>¥ {{ money(totals.depreciation) }}</strong>
      </div>
      <div class="book">
        <span>账面净值</span><strong>¥ {{ money(totals.book) }}</strong>
      </div>
    </div>

    <el-card>
      <div class="toolbar">
        <el-input
          v-model="keyword"
          clearable
          placeholder="资产编号 / 名称"
          style="width: 280px"
          @keyup.enter="load"
        />
        <el-button @click="load">查询</el-button>
      </div>
      <el-table
        :data="rows"
        border
        stripe
      >
        <el-table-column
          prop="anln1"
          label="资产编号"
          width="130"
        />
        <el-table-column
          prop="name"
          label="资产名称"
          min-width="160"
        />
        <el-table-column
          prop="assetClass"
          label="资产类别"
          width="110"
        />
        <el-table-column
          prop="kostl"
          label="成本中心"
          width="110"
        />
        <el-table-column
          prop="capitalizationDate"
          label="资本化日期"
          width="120"
        />
        <el-table-column
          label="原值"
          width="125"
          align="right"
          ><template #default="{ row }">{{ money(row.acquisitionValue) }}</template></el-table-column
        >
        <el-table-column
          label="累计折旧"
          width="125"
          align="right"
          ><template #default="{ row }">{{ money(row.accumulatedDepreciation) }}</template></el-table-column
        >
        <el-table-column
          label="账面净值"
          width="125"
          align="right"
          ><template #default="{ row }">{{ money(row.bookValue) }}</template></el-table-column
        >
        <el-table-column
          label="状态"
          width="100"
          ><template #default="{ row }"><StatusTag :status="row.status" /></template
        ></el-table-column>
        <el-table-column
          label="操作"
          width="160"
          fixed="right"
          ><template #default="{ row }">
            <el-button
              link
              type="primary"
              @click="openAcquisition(row)"
              >购置过账</el-button
            >
            <el-button
              link
              @click="openExplorer(row)"
              >浏览</el-button
            >
          </template></el-table-column
        >
      </el-table>
    </el-card>

    <el-dialog
      v-model="createVisible"
      title="AS01 创建资产主数据"
      width="560px"
    >
      <el-form
        :model="createForm"
        label-width="110px"
      >
        <el-form-item label="资产名称"><el-input v-model="createForm.name" /></el-form-item>
        <el-form-item label="资产类别"
          ><el-select
            v-model="createForm.assetClass"
            style="width: 100%"
            ><el-option
              label="机器设备"
              value="MACHINE" /><el-option
              label="办公设备"
              value="OFFICE" /><el-option
              label="运输工具"
              value="VEHICLE" /></el-select
        ></el-form-item>
        <el-form-item label="公司代码"><el-input v-model="createForm.bukrs" /></el-form-item>
        <el-form-item label="成本中心"><el-input v-model="createForm.kostl" /></el-form-item>
        <el-form-item label="使用寿命(月)"
          ><el-input-number
            v-model="createForm.usefulLifeMonths"
            :min="1"
        /></el-form-item>
        <el-form-item label="预计残值"
          ><el-input-number
            v-model="createForm.salvageValue"
            :min="0"
            :precision="2"
        /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="createVisible = false">取消</el-button
        ><el-button
          type="primary"
          :loading="saving"
          @click="createAsset"
          >保存</el-button
        ></template
      >
    </el-dialog>

    <el-dialog
      v-model="acquisitionVisible"
      title="资产购置过账"
      width="520px"
    >
      <el-alert
        :title="`${selected?.anln1} · ${selected?.name}`"
        type="info"
        :closable="false"
      />
      <el-form
        :model="acquisitionForm"
        label-width="100px"
        style="margin-top: 20px"
      >
        <el-form-item label="购置金额"
          ><el-input-number
            v-model="acquisitionForm.amount"
            :min="0.01"
            :precision="2"
        /></el-form-item>
        <el-form-item label="过账日期"
          ><el-date-picker
            v-model="acquisitionForm.postingDate"
            value-format="YYYY-MM-DD"
        /></el-form-item>
        <el-form-item label="贷方科目"><el-input v-model="acquisitionForm.offsetAccount" /></el-form-item>
        <el-form-item label="摘要"><el-input v-model="acquisitionForm.text" /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="acquisitionVisible = false">取消</el-button
        ><el-button
          type="primary"
          :loading="saving"
          @click="acquire"
          >过账</el-button
        ></template
      >
    </el-dialog>

    <el-dialog
      v-model="depreciationVisible"
      title="AFAB 月度折旧运行"
      width="500px"
    >
      <el-alert
        title="同一资产、同一期间只会过账一次；重复运行会自动跳过。"
        type="warning"
        :closable="false"
      />
      <el-form
        :model="depreciationForm"
        label-width="100px"
        style="margin-top: 20px"
      >
        <el-form-item label="折旧期间"
          ><el-date-picker
            v-model="depreciationForm.period"
            type="month"
            value-format="YYYY-MM"
        /></el-form-item>
        <el-form-item label="过账日期"
          ><el-date-picker
            v-model="depreciationForm.postingDate"
            value-format="YYYY-MM-DD"
        /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="depreciationVisible = false">取消</el-button
        ><el-button
          type="primary"
          :loading="saving"
          @click="runDepreciation"
          >执行正式运行</el-button
        ></template
      >
    </el-dialog>

    <el-drawer
      v-model="explorerVisible"
      title="AW01N 资产浏览器"
      size="620px"
    >
      <el-descriptions
        v-if="selected"
        :column="2"
        border
      >
        <el-descriptions-item label="资产">{{ selected.anln1 }}</el-descriptions-item
        ><el-descriptions-item label="名称">{{ selected.name }}</el-descriptions-item>
        <el-descriptions-item label="原值">{{ money(selected.acquisitionValue) }}</el-descriptions-item
        ><el-descriptions-item label="账面净值">{{ money(selected.bookValue) }}</el-descriptions-item>
      </el-descriptions>
      <el-table
        :data="transactions"
        border
        style="margin-top: 20px"
        ><el-table-column
          prop="postingDate"
          label="过账日期" /><el-table-column
          prop="transactionType"
          label="业务类型" /><el-table-column
          prop="fiscalPeriod"
          label="期间" /><el-table-column
          prop="amount"
          label="金额" /><el-table-column
          prop="belnr"
          label="FI 凭证"
      /></el-table>
    </el-drawer>
  </PageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import { fiApi } from '../../api'

const today = new Date().toISOString().slice(0, 10)
const rows = ref([]),
  keyword = ref(''),
  selected = ref(),
  transactions = ref([]),
  saving = ref(false)
const createVisible = ref(false),
  acquisitionVisible = ref(false),
  depreciationVisible = ref(false),
  explorerVisible = ref(false)
const createForm = reactive({
  name: '',
  assetClass: 'MACHINE',
  bukrs: '1000',
  kostl: 'CC1000',
  usefulLifeMonths: 60,
  salvageValue: 0,
})
const acquisitionForm = reactive({ amount: 0, postingDate: today, offsetAccount: '1002', text: '' })
const depreciationForm = reactive({ period: today.slice(0, 7), postingDate: today })
const totals = computed(() =>
  rows.value.reduce(
    (r, a) => ({
      acquisition: r.acquisition + Number(a.acquisitionValue || 0),
      depreciation: r.depreciation + Number(a.accumulatedDepreciation || 0),
      book: r.book + Number(a.bookValue || 0),
    }),
    { acquisition: 0, depreciation: 0, book: 0 },
  ),
)
const money = (value) =>
  Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
async function load() {
  rows.value = await fiApi.assets({ q: keyword.value })
}
async function createAsset() {
  saving.value = true
  try {
    const asset = await fiApi.createAsset(createForm)
    createVisible.value = false
    ElMessage.success(`资产 ${asset.anln1} 已创建`)
    await load()
  } finally {
    saving.value = false
  }
}
function openAcquisition(row) {
  selected.value = row
  acquisitionForm.amount = 0
  acquisitionVisible.value = true
}
async function acquire() {
  saving.value = true
  try {
    const tx = await fiApi.acquireAsset(selected.value.anln1, acquisitionForm)
    acquisitionVisible.value = false
    ElMessage.success(`购置已过账，FI 凭证 ${tx.belnr}`)
    await load()
  } finally {
    saving.value = false
  }
}
async function runDepreciation() {
  saving.value = true
  try {
    const result = await fiApi.runDepreciation(depreciationForm)
    depreciationVisible.value = false
    ElMessage.success(`折旧完成：${result.postedAssets} 项，金额 ${money(result.amount)}`)
    await load()
  } finally {
    saving.value = false
  }
}
async function openExplorer(row) {
  selected.value = row
  transactions.value = await fiApi.assetTransactions(row.anln1)
  explorerVisible.value = true
}
onMounted(load)
</script>

<style scoped>
.asset-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 18px;
}
.asset-summary > div {
  background: #fff;
  border: 1px solid #dce3ea;
  border-top: 3px solid #8796a5;
  border-radius: 5px;
  padding: 17px 20px;
}
.asset-summary .book {
  border-top-color: #0a6ed1;
}
.asset-summary span {
  display: block;
  color: #687887;
  font-size: 13px;
  margin-bottom: 8px;
}
.asset-summary strong {
  font-size: 23px;
  color: #253746;
}
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
@media (max-width: 900px) {
  .asset-summary {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
