<template>
  <PageShell
    title="外向交货"
    tcode="VL01N"
  >
    <template #actions>
      <el-button
        type="primary"
        @click="visible = true"
        >从销售订单创建</el-button
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
          label="交货单"
        />
        <el-table-column
          prop="soVbeln"
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
        <el-table-column label="BMS同步">
          <template #default="{ row }">
            <StatusTag :value="row.bmsStatus || row.syncStatus" />
            <el-button
              link
              @click="repush(row)"
              >重推</el-button
            >
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button
              link
              :disabled="row.status !== 'OPEN'"
              @click="pick(row)"
              >拣配</el-button
            >
            <el-button
              link
              :disabled="row.status !== 'PICKED'"
              @click="pgi(row)"
              >发货过账</el-button
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
      title="从销售订单创建交货单"
      width="650px"
    >
      <el-select
        v-model="form.soVbeln"
        placeholder="选择开放销售订单"
        @change="loadSo"
      >
        <el-option
          v-for="order in openSos"
          :key="order.vbeln"
          :label="`${order.vbeln} ${order.kunnr}`"
          :value="order.vbeln"
        />
      </el-select>

      <el-table
        :data="form.items"
        border
        class="mt"
      >
        <el-table-column
          prop="matnr"
          label="物料"
        />
        <el-table-column
          prop="openQty"
          label="未交数量"
        />
        <el-table-column label="本次数量">
          <template #default="{ row }">
            <el-input-number
              v-model="row.qty"
              :min="0"
              :max="row.openQty"
            />
          </template>
        </el-table-column>
      </el-table>

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
      title="交货单明细"
      size="60%"
    >
      <el-descriptions
        v-if="selected"
        :column="3"
        border
      >
        <el-descriptions-item label="交货单">{{ selected.vbeln }}</el-descriptions-item>
        <el-descriptions-item label="销售订单">{{ selected.soVbeln }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ selected.kunnr }}</el-descriptions-item>
        <el-descriptions-item label="工厂">{{ selected.werks }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ selected.status }}</el-descriptions-item>
        <el-descriptions-item label="BMS同步">
          {{ selected.bmsStatus || selected.syncStatus || '-' }}
        </el-descriptions-item>
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
          prop="qty"
          label="交货数量"
        />
        <el-table-column
          prop="pickedQty"
          label="拣配数量"
        />
        <el-table-column
          prop="pgiQty"
          label="PGI数量"
        />
      </el-table>
    </el-drawer>
  </PageShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import { sdApi } from '../../api'

const rows = ref([])
const openSos = ref([])
const visible = ref(false)
const drawer = ref(false)
const selected = ref(null)
const form = reactive({ soVbeln: '', items: [] })

async function load() {
  rows.value = await sdApi.dns()
  openSos.value = (await sdApi.sos()).filter((item) => item.status === 'OPEN')
}

async function loadSo() {
  const order = await sdApi.so(form.soVbeln)
  form.items = (order.items || []).map((item) => {
    const openQty = item.kwmeng - (item.deliveredQty || 0)
    return {
      matnr: item.matnr,
      openQty,
      qty: openQty,
    }
  })
}

async function create() {
  await sdApi.createDn(form)
  visible.value = false
  await load()
}

async function pick(row) {
  await sdApi.pick(row.vbeln)
  await load()
}

async function pgi(row) {
  await sdApi.pgi(row.vbeln)
  await load()
}

async function repush(row) {
  await sdApi.repush(row.vbeln)
  await load()
}

async function show(row) {
  selected.value = await sdApi.dn(row.vbeln)
  drawer.value = true
}

onMounted(load)
</script>
