<template>
  <PageShell
    title="销售订单"
    tcode="VA01"
  >
    <template #actions>
      <el-button
        type="primary"
        @click="visible = true"
        >创建销售订单</el-button
      >
    </template>

    <el-card>
      <el-table
        :data="rows"
        border
        stripe
      >
        <el-table-column
          prop="vbeln"
          label="销售订单"
        />
        <el-table-column
          prop="kunnr"
          label="客户"
        />
        <el-table-column
          prop="status"
          label="状态"
        >
          <template #default="{ row }">
            <StatusTag :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column
          prop="totalAmount"
          label="金额"
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

    <el-dialog
      v-model="visible"
      title="创建销售订单"
      width="760px"
    >
      <el-form
        :model="form"
        inline
      >
        <el-form-item label="客户">
          <el-select
            v-model="form.kunnr"
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
        </el-form-item>
        <el-form-item label="销售组织">
          <el-input v-model="form.vkorg" />
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
              @change="price(row)"
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
              v-model="row.kwmeng"
              :min="1"
            />
          </template>
        </el-table-column>
        <el-table-column label="单价">
          <template #default="{ row }">
            <el-input-number
              v-model="row.netpr"
              :min="0"
            />
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
      v-model="drawer"
      title="销售订单明细"
      size="60%"
    >
      <el-descriptions
        v-if="selected"
        :column="3"
        border
      >
        <el-descriptions-item label="销售订单">{{ selected.vbeln }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ selected.kunnr }}</el-descriptions-item>
        <el-descriptions-item label="销售组织">{{ selected.vkorg }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ selected.status }}</el-descriptions-item>
        <el-descriptions-item label="币种">{{ selected.waers }}</el-descriptions-item>
      </el-descriptions>
      <el-table
        v-if="selected"
        :data="selected.items || []"
        border
        class="drawer-table"
      >
        <el-table-column
          prop="posnr"
          label="行号"
        />
        <el-table-column
          prop="matnr"
          label="物料"
        />
        <el-table-column
          prop="kwmeng"
          label="订单数量"
        />
        <el-table-column
          prop="deliveredQty"
          label="已交货"
        />
        <el-table-column
          prop="billedQty"
          label="已开票"
        />
        <el-table-column
          prop="netpr"
          label="单价"
        />
      </el-table>
    </el-drawer>
  </PageShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import { mmApi, sdApi } from '../../api'

const rows = ref([])
const visible = ref(false)
const drawer = ref(false)
const selected = ref(null)
const materials = ref([])
const customers = ref([])
const form = reactive({
  kunnr: '200010',
  vkorg: '1000',
  items: [{ matnr: 'M1001', kwmeng: 1, netpr: 45 }],
})

async function load() {
  rows.value = await sdApi.sos()
}

function add() {
  form.items.push({ matnr: 'M1001', kwmeng: 1, netpr: 45 })
}

function price(row) {
  const material = materials.value.find((item) => item.matnr === row.matnr)
  if (material) {
    row.netpr = material.stdPrice
  }
}

async function save() {
  await sdApi.createSo(form)
  visible.value = false
  await load()
}

async function show(row) {
  selected.value = await sdApi.so(row.vbeln)
  drawer.value = true
}

onMounted(async () => {
  await load()
  materials.value = (await mmApi.materials({ page: 1, size: 100 })).records || []
  customers.value = (await sdApi.customers({ page: 1, size: 100 })).records || []
})
</script>
