<template>
  <PageShell
    title="总账科目"
    tcode="FS00"
    ><template #actions
      ><el-button
        type="primary"
        @click="open()"
        >新建科目</el-button
      ></template
    ><el-card
      ><div class="toolbar">
        <el-input
          v-model="q"
          placeholder="科目/名称"
          @keyup.enter="load"
        /><el-button @click="load">查询</el-button>
      </div>
      <el-table
        :data="rows"
        border
        stripe
        ><el-table-column
          prop="saknr"
          label="科目"
        /><el-table-column
          prop="txt"
          label="名称"
        /><el-table-column
          prop="type"
          label="类型"
        /><el-table-column
          prop="reconType"
          label="统驭类型"
        /><el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button
              link
              @click="open(row)"
              >编辑</el-button
            ></template
          ></el-table-column
        ></el-table
      ></el-card
    ><el-dialog
      v-model="visible"
      title="总账科目"
      ><el-form
        :model="form"
        label-width="95px"
        ><el-form-item label="科目"
          ><el-input
            v-model="form.saknr"
            :disabled="editing" /></el-form-item
        ><el-form-item label="名称"><el-input v-model="form.txt" /></el-form-item
        ><el-form-item label="类型"
          ><el-select v-model="form.type"
            ><el-option
              v-for="x in ['ASSET', 'LIABILITY', 'EXPENSE', 'REVENUE', 'EQUITY']"
              :key="x"
              :label="x"
              :value="x" /></el-select></el-form-item
        ><el-form-item label="统驭类型"><el-input v-model="form.reconType" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
          >保存</el-button
        ></template
      ></el-dialog
    ></PageShell
  >
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import PageShell from '../../components/PageShell.vue'
import { fiApi } from '../../api'
const rows = ref([])
const q = ref('')
const visible = ref(false)
const editing = ref(false)
const form = reactive({})
async function load() {
  const r = await fiApi.gl({ q: q.value, page: 1, size: 50 })
  rows.value = r.records || r || []
}
function open(r) {
  editing.value = !!r
  Object.assign(form, r || { saknr: '', txt: '', type: 'ASSET', reconType: '' })
  visible.value = true
}
async function save() {
  editing.value ? await fiApi.updateGl(form.id, form) : await fiApi.createGl(form)
  visible.value = false
  load()
}
onMounted(load)
</script>
