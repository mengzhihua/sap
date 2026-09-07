<template>
  <PageShell
    title="生产订单"
    tcode="CO01"
  >
    <template #actions>
      <el-button
        type="primary"
        @click="visible = true"
        >创建生产订单</el-button
      >
    </template>

    <el-card>
      <el-table
        :data="rows"
        border
        stripe
      >
        <el-table-column
          prop="aufnr"
          label="生产订单"
        />
        <el-table-column
          prop="matnr"
          label="成品"
        />
        <el-table-column
          prop="werks"
          label="工厂"
        />
        <el-table-column
          prop="gamng"
          label="数量"
        />
        <el-table-column
          prop="plannedCost"
          label="计划成本"
        />
        <el-table-column label="状态">
          <template #default="{ row }">
            <StatusTag :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="300"
        >
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'CRTD'"
              link
              @click="action(row, 'release')"
              >下达</el-button
            >
            <template v-if="['REL', 'CNF'].includes(row.status)">
              <el-button
                link
                @click="action(row, 'issue')"
                >发料</el-button
              >
              <el-button
                link
                @click="action(row, 'confirm')"
                >报工</el-button
              >
              <el-button
                link
                @click="action(row, 'receipt')"
                >完工收货</el-button
              >
            </template>
            <el-button
              v-if="row.status === 'TECO'"
              link
              @click="action(row, 'teco')"
              >技术完成</el-button
            >
            <el-button
              link
              @click="show(row)"
              >明细</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="visible"
      title="创建生产订单"
    >
      <el-form
        :model="form"
        label-width="90px"
      >
        <el-form-item label="成品物料">
          <el-select
            v-model="form.matnr"
            filterable
            placeholder="选择成品物料"
          >
            <el-option
              v-for="material in materials"
              :key="material.matnr"
              :label="`${material.matnr} ${material.maktx}`"
              :value="material.matnr"
            />
          </el-select>
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
        <el-form-item label="数量">
          <el-input-number
            v-model="form.targetQty"
            :min="1"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button
          type="primary"
          @click="create"
          >创建</el-button
        >
      </template>
    </el-dialog>

    <el-drawer
      v-model="drawer"
      title="生产订单明细"
      size="65%"
    >
      <el-descriptions
        v-if="selected"
        :column="3"
        border
      >
        <el-descriptions-item label="生产订单">{{ selected.aufnr }}</el-descriptions-item>
        <el-descriptions-item label="成品">{{ selected.matnr }}</el-descriptions-item>
        <el-descriptions-item label="工厂">{{ selected.werks }}</el-descriptions-item>
        <el-descriptions-item label="计划成本">{{ selected.plannedCost }}</el-descriptions-item>
        <el-descriptions-item label="实际成本">{{ selected.actualCost }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ selected.status }}</el-descriptions-item>
      </el-descriptions>
      <el-table
        v-if="selected"
        :data="selected.components || []"
        border
        class="drawer-table"
      >
        <el-table-column
          prop="component"
          label="组件"
        />
        <el-table-column
          prop="requiredQty"
          label="需求数量"
        />
        <el-table-column
          prop="issuedQty"
          label="已发料"
        />
        <el-table-column
          prop="plannedCost"
          label="计划成本"
        />
        <el-table-column
          prop="actualCost"
          label="实际成本"
        />
      </el-table>
    </el-drawer>
  </PageShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import PageShell from '../../components/PageShell.vue'
import { basisApi } from '../../api/basis'
import { mmApi } from '../../api/mm'
import StatusTag from '../../components/StatusTag.vue'
import { ppApi } from '../../api'

const rows = ref([])
const materials = ref([])
const plants = ref([])
const visible = ref(false)
const drawer = ref(false)
const selected = ref(null)
const form = reactive({ matnr: 'F2001', werks: '1000', targetQty: 1 })

async function load() {
  rows.value = await ppApi.orders()
}

async function create() {
  await ppApi.createOrder(form)
  visible.value = false
  await load()
}

async function action(row, actionName) {
  let params = {}
  if (['confirm', 'receipt'].includes(actionName)) {
    params = await ElMessageBox.prompt('请输入数量', '数量', {
      inputValue: String(row.gamng || 1),
    })
      .then((result) => ({ value: result.value }))
      .catch(() => null)
  }
  if (params === null) {
    return
  }
  await ppApi.action(row.aufnr, actionName, params)
  await load()
}

async function show(row) {
  selected.value = await ppApi.order(row.aufnr)
  drawer.value = true
}

onMounted(async () => {
  await load()
  materials.value = (await mmApi.materials({ page: 1, size: 100 })).records || []
  plants.value = await basisApi.org('plants')
})
</script>
