<template>
  <PageShell
    title="采购申请"
    tcode="ME51N"
  >
    <template #actions>
      <el-button
        type="primary"
        @click="visible = true"
        >创建采购申请</el-button
      >
    </template>

    <el-card>
      <el-table
        :data="rows"
        border
        stripe
      >
        <el-table-column
          prop="banfn"
          label="申请号"
        />
        <el-table-column
          prop="requester"
          label="申请人"
        />
        <el-table-column
          prop="werks"
          label="工厂"
        />
        <el-table-column
          prop="status"
          label="状态"
        />
        <el-table-column
          prop="totalAmount"
          label="总金额"
        />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button
              link
              :disabled="row.status === 'ORDERED'"
              @click="release(row)"
              >释放</el-button
            >
            <el-button
              link
              @click="detail = row"
              >明细</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="visible"
      title="创建采购申请"
      width="760px"
    >
      <el-form
        :model="form"
        inline
      >
        <el-form-item label="申请人">
          <el-input v-model="form.requester" />
        </el-form-item>
        <el-form-item label="工厂">
          <el-select
            v-model="form.werks"
            filterable
          >
            <el-option
              v-for="plant in plants"
              :key="plant.werks"
              :label="plant.werks"
              :value="plant.werks"
            />
          </el-select>
        </el-form-item>
      </el-form>

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
              @change="fillPrice(row)"
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
        <el-table-column label="数量">
          <template #default="{ row }">
            <el-input-number
              v-model="row.menge"
              :min="1"
            />
          </template>
        </el-table-column>
        <el-table-column label="价格">
          <template #default="{ row }">
            <el-input-number
              v-model="row.netpr"
              :min="0"
            />
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
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button
          type="primary"
          @click="save"
          >提交</el-button
        >
      </template>
    </el-dialog>

    <el-drawer
      v-model="detail"
      title="采购申请明细"
      size="55%"
    >
      <el-descriptions
        v-if="detail"
        :column="2"
        border
      >
        <el-descriptions-item label="申请号">{{ detail.banfn }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.requester }}</el-descriptions-item>
        <el-descriptions-item label="工厂">{{ detail.werks }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
      </el-descriptions>
      <el-table
        v-if="detail"
        :data="detail.items || []"
        border
        class="drawer-table"
      >
        <el-table-column
          prop="banfn"
          label="申请号"
        />
        <el-table-column
          prop="bnfpo"
          label="行号"
        />
        <el-table-column
          prop="matnr"
          label="物料"
        />
        <el-table-column
          prop="menge"
          label="数量"
        />
        <el-table-column
          prop="netpr"
          label="单价"
        />
        <el-table-column
          prop="werks"
          label="工厂"
        />
      </el-table>
    </el-drawer>
  </PageShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageShell from '../../components/PageShell.vue'
import { basisApi } from '../../api/basis'
import { mmApi } from '../../api'

const rows = ref([])
const materials = ref([])
const plants = ref([])
const visible = ref(false)
const detail = ref(null)
const form = reactive({ requester: '', bukrs: '1000', werks: '1000', items: [] })

function add() {
  form.items.push({ matnr: 'M1001', menge: 1, netpr: 45, werks: form.werks, lgort: '0001' })
}

function fillPrice(row) {
  const material = materials.value.find((item) => item.matnr === row.matnr)
  if (material) {
    row.netpr = material.stdPrice
  }
}

async function load() {
  rows.value = await mmApi.prs()
}

async function save() {
  await mmApi.createPr(form)
  visible.value = false
  form.items = []
  await load()
}

async function release(row) {
  await mmApi.releasePr(row.banfn)
  ElMessage.success('已释放')
  await load()
}

onMounted(async () => {
  add()
  materials.value = (await mmApi.materials({ page: 1, size: 100 })).records || []
  plants.value = await basisApi.org('plants')
  await load()
})
</script>
