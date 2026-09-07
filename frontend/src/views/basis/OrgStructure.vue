<template>
  <PageShell
    title="组织结构"
    tcode="SPRO"
    ><el-card
      ><el-tabs v-model="active"
        ><el-tab-pane
          v-for="tab in tabs"
          :key="tab.key"
          :label="tab.label"
          :name="tab.key"
          ><el-button
            type="primary"
            class="tab-action"
            @click="open(tab)"
            >新增{{ tab.label }}</el-button
          ><el-table
            :data="rows[tab.key] || []"
            border
            stripe
            ><el-table-column
              v-for="column in columns(tab.key)"
              :key="column.prop"
              :prop="column.prop"
              :label="column.label" /></el-table></el-tab-pane></el-tabs></el-card
    ><el-dialog
      v-model="visible"
      :title="`新增${current?.label || ''}`"
      ><el-form
        :model="form"
        label-width="100px"
        ><el-form-item
          v-for="field in fields(current?.key)"
          :key="field"
          :label="field"
          ><el-input v-model="form[field]" /></el-form-item></el-form
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
import { basisApi } from '../../api'
const tabs = [
    { key: 'company-codes', label: '公司代码' },
    { key: 'plants', label: '工厂' },
    { key: 'storage-locations', label: '库存地点' },
    { key: 'purchasing-orgs', label: '采购组织' },
    { key: 'purchasing-groups', label: '采购组' },
    { key: 'sales-orgs', label: '销售组织' },
  ]
const active = ref('company-codes')
const rows = reactive({})
const visible = ref(false)
const current = ref(null)
const form = reactive({})
const mapping = {
  'company-codes': [
    ['bukrs', '代码'],
    ['name', '名称'],
    ['currency', '货币'],
  ],
  plants: [
    ['werks', '工厂'],
    ['name', '名称'],
    ['bukrs', '公司代码'],
  ],
  'storage-locations': [
    ['werks', '工厂'],
    ['lgort', '库存地点'],
    ['name', '名称'],
  ],
  'purchasing-orgs': [
    ['ekorg', '采购组织'],
    ['name', '名称'],
  ],
  'purchasing-groups': [
    ['ekgrp', '采购组'],
    ['name', '名称'],
  ],
  'sales-orgs': [
    ['vkorg', '销售组织'],
    ['name', '名称'],
    ['bukrs', '公司代码'],
  ],
}
function fields(k) {
  return (mapping[k] || []).map((x) => x[0])
}
function columns(k) {
  return (mapping[k] || []).map((x) => ({ prop: x[0], label: x[1] }))
}
async function load(k) {
  rows[k] = await basisApi.org(k)
}
function open(t) {
  current.value = t
  Object.assign(form, {})
  visible.value = true
}
async function save() {
  await basisApi.createOrg(current.value.key, form)
  visible.value = false
  load(current.value.key)
}
onMounted(() => tabs.forEach((x) => load(x.key)))
</script>
