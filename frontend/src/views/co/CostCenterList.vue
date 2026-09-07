<template>
  <PageShell
    title="成本中心"
    tcode="KS01"
    ><template #actions
      ><el-button
        type="primary"
        @click="open()"
        >新建成本中心</el-button
      ></template
    ><el-card
      ><el-table
        :data="rows"
        border
        stripe
        ><el-table-column
          prop="kostl"
          label="成本中心"
        /><el-table-column
          prop="name"
          label="名称"
        /><el-table-column
          prop="bukrs"
          label="公司代码"
        /><el-table-column
          prop="responsible"
          label="负责人"
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
      title="成本中心"
      ><el-form
        :model="form"
        label-width="90px"
        ><el-form-item label="成本中心"
          ><el-input
            v-model="form.kostl"
            :disabled="editing" /></el-form-item
        ><el-form-item label="名称"><el-input v-model="form.name" /></el-form-item
        ><el-form-item label="公司代码"><el-input v-model="form.bukrs" /></el-form-item
        ><el-form-item label="负责人"><el-input v-model="form.responsible" /></el-form-item></el-form
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
import { coApi } from '../../api'
const rows = ref([])
const visible = ref(false)
const editing = ref(false)
const form = reactive({})
async function load() {
  rows.value = await coApi.centers()
}
function open(r) {
  editing.value = !!r
  Object.assign(form, r || { kostl: '', name: '', bukrs: '1000', responsible: '' })
  visible.value = true
}
async function save() {
  editing.value ? await coApi.updateCenter(form.kostl, form) : await coApi.createCenter(form)
  visible.value = false
  load()
}
onMounted(load)
</script>
