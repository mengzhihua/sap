<template>
  <PageShell title="物料主数据" tcode="MM01">
    <template #actions><el-button type="primary" @click="open()">新建物料</el-button></template>
    <el-card><div class="toolbar"><el-input v-model="query.q" clearable placeholder="物料/名称/别名" @keyup.enter="load" /><el-button @click="load">搜索</el-button></div>
      <el-table :data="rows" stripe border><el-table-column prop="matnr" label="物料号" /><el-table-column prop="maktx" label="物料描述" /><el-table-column prop="meins" label="基本单位" width="100" /><el-table-column prop="mtart" label="物料类型" width="100" /><el-table-column prop="matkl" label="物料组" /><el-table-column prop="stdPrice" label="标准价" /><el-table-column prop="priceControl" label="价格控制" /><el-table-column prop="aliasCode" label="外部编码" /><el-table-column label="操作" width="90"><template #default="{ row }"><el-button link @click="open(row)">编辑</el-button></template></el-table-column></el-table>
      <TablePager v-bind="query" :total="total" @change="changePage" @size="changeSize" />
    </el-card>
    <el-dialog v-model="visible" :title="editing ? '编辑物料' : '新建物料'" width="520px"><el-form :model="form" label-width="100px"><el-form-item label="物料号"><el-input v-model="form.matnr" :disabled="editing" /></el-form-item><el-form-item label="描述"><el-input v-model="form.maktx" /></el-form-item><el-form-item label="基本单位"><el-input v-model="form.meins" /></el-form-item><el-form-item label="物料类型"><el-select v-model="form.mtart"><el-option v-for="x in ['ROH','HALB','FERT','HAWA']" :key="x" :label="x" :value="x" /></el-select></el-form-item><el-form-item label="物料组"><el-input v-model="form.matkl" /></el-form-item><el-form-item label="标准价"><el-input-number v-model="form.stdPrice" :min="0" /></el-form-item><el-form-item label="价格控制"><el-input v-model="form.priceControl" /></el-form-item><el-form-item label="外部编码"><el-input v-model="form.aliasCode" /></el-form-item></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
  </PageShell>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'; import TablePager from '../../components/TablePager.vue'; import { mmApi } from '../../api'
const rows = ref([]); const total = ref(0); const visible = ref(false); const editing = ref(false); const form = reactive({}); const query = reactive({ q: '', page: 1, size: 20 })
async function load() { const result = await mmApi.materials(query); rows.value = result.records || []; total.value = result.total || 0 }
function open(row) { editing.value = !!row; Object.assign(form, row || { matnr: '', maktx: '', meins: 'EA', mtart: 'ROH', matkl: '', stdPrice: 0, priceControl: 'S', aliasCode: '' }); visible.value = true }
async function save() { if (editing.value) await mmApi.updateMaterial(form.matnr, form); else await mmApi.createMaterial(form); visible.value = false; await load() }
function changePage(v) { query.page = v; load() }; function changeSize(v) { query.size = v; query.page = 1; load() }; onMounted(load)
</script>
