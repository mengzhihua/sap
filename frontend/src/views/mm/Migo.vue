<template>
  <PageShell
    title="货物移动"
    tcode="MIGO"
  >
    <el-card>
      <el-radio-group v-model="form.bwart">
        <el-radio-button value="101">101 收货</el-radio-button>
        <el-radio-button value="201">201 成本中心发料</el-radio-button>
        <el-radio-button value="311">311 库存转储</el-radio-button>
      </el-radio-group>

      <div
        v-if="form.bwart === '101'"
        class="ref-bar"
      >
        <el-input
          v-model="form.refNo"
          placeholder="采购订单号"
        />
        <el-button
          type="primary"
          @click="loadPo"
          >加载开放项目</el-button
        >
      </div>

      <el-table
        :data="form.items"
        border
      >
        <el-table-column
          label="物料"
          min-width="220"
        >
          <template #default="{ row }">
            <el-select
              v-model="row.matnr"
              filterable
              placeholder="选择物料"
            >
              <el-option
                v-for="material in materials"
                :key="material.matnr"
                :label="`${material.matnr} ${material.maktx}`"
                :value="material.matnr"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="工厂">
          <template #default="{ row }">
            <el-select
              v-model="row.werks"
              filterable
            >
              <el-option
                v-for="plant in plants"
                :key="plant.werks"
                :label="plant.werks"
                :value="plant.werks"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="库存地点">
          <template #default="{ row }">
            <el-select
              v-model="row.lgort"
              filterable
            >
              <el-option
                v-for="location in locationsFor(row.werks)"
                :key="location.lgort"
                :label="location.lgort"
                :value="location.lgort"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column
          v-if="form.bwart === '311'"
          label="目标库存地点"
        >
          <template #default="{ row }">
            <el-select
              v-model="row.toLgort"
              filterable
            >
              <el-option
                v-for="location in locationsFor(row.werks)"
                :key="location.lgort"
                :label="location.lgort"
                :value="location.lgort"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column
          v-if="form.bwart === '201'"
          label="成本中心"
        >
          <template #default="{ row }">
            <el-select
              v-model="row.kostl"
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
        <el-table-column label="数量">
          <template #default="{ row }">
            <el-input-number
              v-model="row.qty"
              :min="0.001"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ $index }">
            <el-button
              link
              @click="form.items.splice($index, 1)"
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
      <el-button
        type="primary"
        @click="post"
        >过账</el-button
      >
    </el-card>

    <el-dialog
      v-model="resultVisible"
      title="物料凭证过账结果"
    >
      <el-result
        icon="success"
        title="过账成功"
        :sub-title="`物料凭证 ${result?.mblnr || result?.materialDocument} / FI ${result?.fiBelnr || '-'}`"
      />
    </el-dialog>
  </PageShell>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import PageShell from '../../components/PageShell.vue'
import { basisApi } from '../../api/basis'
import { coApi } from '../../api/co'
import { mmApi } from '../../api'

const form = reactive({
  bwart: '101',
  refType: 'PO',
  refNo: '',
  items: [],
})
const centers = ref([])
const materials = ref([])
const plants = ref([])
const locations = ref([])
const result = ref(null)
const resultVisible = ref(false)

function add() {
  form.items.push({
    matnr: 'M1001',
    werks: '1000',
    lgort: '0001',
    toLgort: '0002',
    kostl: '',
    qty: 1,
  })
}

function locationsFor(werks) {
  return locations.value.filter((location) => !location.werks || location.werks === werks)
}

async function loadPo() {
  const po = await mmApi.po(form.refNo)
  form.items = (po.items || [])
    .filter((item) => (item.menge || 0) - (item.deliveredQty || 0) > 0)
    .map((item) => ({
      matnr: item.matnr,
      werks: item.werks || '1000',
      lgort: item.lgort || '0001',
      ebelp: item.ebelp,
      qty: (item.menge || 0) - (item.deliveredQty || 0),
    }))
  ElMessage.success('已加载开放项目')
}

async function post() {
  result.value = await mmApi.migo(form)
  resultVisible.value = true
}

watch(
  () => form.bwart,
  () => {
    form.items = []
    add()
  },
)

onMounted(async () => {
  add()
  centers.value = await coApi.centers()
  materials.value = (await mmApi.materials({ page: 1, size: 100 })).records || []
  plants.value = await basisApi.org('plants')
  locations.value = await basisApi.org('storage-locations')
})
</script>
