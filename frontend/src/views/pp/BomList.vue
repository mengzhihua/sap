<template>
  <PageShell
    title="物料清单"
    tcode="CS01"
  >
    <template #actions>
      <el-button
        type="primary"
        @click="visible = true"
        >维护BOM</el-button
      >
    </template>

    <el-card>
      <div class="toolbar">
        <el-select
          v-model="matnr"
          filterable
          placeholder="成品物料"
        >
          <el-option
            v-for="material in materials"
            :key="material.matnr"
            :label="`${material.matnr} ${material.maktx}`"
            :value="material.matnr"
          />
        </el-select>
        <el-select
          v-model="werks"
          filterable
          placeholder="工厂"
        >
          <el-option
            v-for="plant in plants"
            :key="plant.werks"
            :label="plant.werks"
            :value="plant.werks"
          />
        </el-select>
        <el-button @click="load">查询</el-button>
      </div>
      <el-table
        :data="rows"
        border
        stripe
      >
        <el-table-column
          prop="matnr"
          label="成品"
        />
        <el-table-column
          prop="werks"
          label="工厂"
        />
        <el-table-column
          prop="baseQty"
          label="基准数量"
        />
        <el-table-column
          prop="component"
          label="组件"
        />
        <el-table-column
          prop="qty"
          label="组件数量"
        />
      </el-table>
    </el-card>

    <el-dialog
      v-model="visible"
      title="维护BOM"
      width="650px"
    >
      <el-form
        :model="form"
        inline
      >
        <el-form-item label="成品">
          <el-select
            v-model="form.matnr"
            filterable
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
      </el-form>

      <el-table
        :data="form.items"
        border
      >
        <el-table-column
          label="组件"
          min-width="220"
        >
          <template #default="{ row }">
            <el-select
              v-model="row.component"
              filterable
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
              v-model="row.qty"
              :min="0"
            />
          </template>
        </el-table-column>
      </el-table>

      <el-button
        class="add-line"
        @click="add"
        >添加组件</el-button
      >
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button
          type="primary"
          @click="save"
          >保存</el-button
        >
      </template>
    </el-dialog>
  </PageShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import { basisApi } from '../../api/basis'
import { mmApi } from '../../api/mm'
import { ppApi } from '../../api'

const rows = ref([])
const materials = ref([])
const plants = ref([])
const matnr = ref('F2001')
const werks = ref('1000')
const visible = ref(false)
const form = reactive({ matnr: 'F2001', werks: '1000', baseQty: 1, items: [] })

function add() {
  form.items.push({ component: 'M1001', qty: 1 })
}

async function load() {
  const result = await ppApi.bom(matnr.value, werks.value)
  rows.value = result.items || result || []
}

async function save() {
  await ppApi.createBom(form)
  visible.value = false
  await load()
}

onMounted(async () => {
  add()
  materials.value = (await mmApi.materials({ page: 1, size: 100 })).records || []
  plants.value = await basisApi.org('plants')
  await load()
})
</script>
