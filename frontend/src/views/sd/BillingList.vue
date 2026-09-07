<template>
  <PageShell
    title="开票"
    tcode="VF01"
  >
    <template #actions>
      <el-button
        type="primary"
        @click="visible = true"
        >创建发票</el-button
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
          label="发票"
        />
        <el-table-column
          prop="dnVbeln"
          label="交货单"
        />
        <el-table-column
          prop="kunnr"
          label="客户"
        />
        <el-table-column
          prop="belnr"
          label="FI凭证"
        />
        <el-table-column
          prop="gross"
          label="含税金额"
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
      title="从发货交货单创建发票"
    >
      <el-select
        v-model="form.dnVbeln"
        placeholder="选择已PGI交货单"
      >
        <el-option
          v-for="delivery in deliveries"
          :key="delivery.vbeln"
          :label="delivery.vbeln"
          :value="delivery.vbeln"
        />
      </el-select>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button
          type="primary"
          @click="create"
          >过账</el-button
        >
      </template>
    </el-dialog>

    <el-drawer
      v-model="drawer"
      title="开票明细"
    >
      <el-descriptions
        v-if="selected"
        :column="3"
        border
      >
        <el-descriptions-item label="发票">{{ selected.vbeln }}</el-descriptions-item>
        <el-descriptions-item label="交货单">{{ selected.dnVbeln }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ selected.kunnr }}</el-descriptions-item>
        <el-descriptions-item label="FI凭证">{{ selected.belnr }}</el-descriptions-item>
        <el-descriptions-item label="含税金额">{{ selected.gross }}</el-descriptions-item>
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
          label="数量"
        />
        <el-table-column
          prop="netpr"
          label="单价"
        />
        <el-table-column
          prop="amount"
          label="金额"
        />
      </el-table>
    </el-drawer>
  </PageShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import { sdApi } from '../../api'

const rows = ref([])
const deliveries = ref([])
const visible = ref(false)
const drawer = ref(false)
const selected = ref(null)
const form = reactive({ dnVbeln: '' })

async function load() {
  rows.value = await sdApi.billings()
  deliveries.value = (await sdApi.dns()).filter((item) => item.status === 'PGI')
}

async function create() {
  await sdApi.createBilling(form)
  visible.value = false
  await load()
}

async function show(row) {
  selected.value = await sdApi.billing(row.vbeln)
  drawer.value = true
}

onMounted(load)
</script>
