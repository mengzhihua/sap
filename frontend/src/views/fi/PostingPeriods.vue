<template>
  <PageShell
    title="打开和关闭过账期间"
    tcode="OB52"
  >
    <template #actions
      ><el-button
        type="primary"
        @click="edit()"
        >新增期间</el-button
      ></template
    >
    <el-alert
      title="配置公司代码和年度后，仅允许在启用的期间内生成 FI 凭证；未配置的年度默认开放。"
      type="info"
      show-icon
      :closable="false"
    />
    <el-card class="mt">
      <el-table
        :data="rows"
        border
      >
        <el-table-column
          prop="bukrs"
          label="公司代码"
          width="130"
        />
        <el-table-column
          prop="fiscalYear"
          label="会计年度"
          width="130"
        />
        <el-table-column label="允许期间"
          ><template #default="{ row }">{{ row.fromPeriod }} — {{ row.toPeriod }}</template></el-table-column
        >
        <el-table-column
          label="状态"
          width="110"
          ><template #default="{ row }"><StatusTag :status="row.open ? 'OPEN' : 'CLOSED'" /></template
        ></el-table-column>
        <el-table-column
          label="操作"
          width="150"
          ><template #default="{ row }"
            ><el-button
              link
              type="primary"
              @click="edit(row)"
              >编辑</el-button
            ><el-button
              link
              type="danger"
              @click="remove(row)"
              >删除</el-button
            ></template
          ></el-table-column
        >
      </el-table>
    </el-card>
    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑过账期间' : '新增过账期间'"
      width="480px"
    >
      <el-form label-width="100px">
        <el-form-item label="公司代码"><el-input v-model="form.bukrs" /></el-form-item>
        <el-form-item label="会计年度"
          ><el-input-number
            v-model="form.fiscalYear"
            :min="2000"
            :max="9999"
        /></el-form-item>
        <el-form-item label="起始期间"
          ><el-input-number
            v-model="form.fromPeriod"
            :min="1"
            :max="12"
        /></el-form-item>
        <el-form-item label="结束期间"
          ><el-input-number
            v-model="form.toPeriod"
            :min="1"
            :max="12"
        /></el-form-item>
        <el-form-item label="允许过账"><el-switch v-model="form.open" /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
          >保存</el-button
        ></template
      >
    </el-dialog>
  </PageShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import { fiApi } from '../../api'

const rows = ref([])
const visible = ref(false)
const defaults = () => ({
  id: null,
  bukrs: '1000',
  fiscalYear: new Date().getFullYear(),
  fromPeriod: 1,
  toPeriod: 12,
  open: true,
})
const form = reactive(defaults())
async function load() {
  rows.value = await fiApi.postingPeriods()
}
function edit(row) {
  Object.assign(form, defaults(), row || {})
  visible.value = true
}
async function save() {
  await fiApi.savePostingPeriod(form)
  visible.value = false
  await load()
}
async function remove(row) {
  await ElMessageBox.confirm('确定删除该期间配置？', '删除期间')
  await fiApi.deletePostingPeriod(row.id)
  await load()
}
onMounted(load)
</script>
