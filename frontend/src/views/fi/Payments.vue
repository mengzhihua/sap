<template>
  <PageShell
    title="付款 / 收款清账"
    tcode="F-53"
    ><el-card
      ><el-form inline
        ><el-form-item label="业务类型"
          ><el-radio-group
            v-model="form.type"
            @change="load"
            ><el-radio-button value="AP">F-53 付款</el-radio-button
            ><el-radio-button value="AR">F-28 收款</el-radio-button></el-radio-group
          ></el-form-item
        ><el-form-item label="伙伴"
          ><el-input
            v-model="form.partner"
            @change="load" /></el-form-item></el-form
      ><el-table
        :data="items"
        border
        @selection-change="selected = $event"
        ><el-table-column
          type="selection"
          width="50" /><el-table-column
          prop="belnr"
          label="凭证号" /><el-table-column
          prop="partner"
          label="伙伴" /><el-table-column
          prop="amount"
          label="金额" /><el-table-column
          prop="budat"
          label="日期"
      /></el-table>
      <div class="balance-footer">
        已选金额：<strong>{{ amount.toFixed(2) }}</strong
        ><el-button
          type="primary"
          :disabled="!selected.length"
          @click="pay"
          >过账清账</el-button
        >
      </div></el-card
    ></PageShell
  >
</template>
<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import PageShell from '../../components/PageShell.vue'
import { fiApi } from '../../api'
const form = reactive({ type: 'AP', partner: '' })
const items = ref([])
const selected = ref([])
const amount = computed(() => selected.value.reduce((a, x) => a + Number(x.amount || 0), 0))
async function load() {
  items.value =
    form.type === 'AP' ? await fiApi.ap({ partner: form.partner }) : await fiApi.ar({ partner: form.partner })
}
async function pay() {
  await fiApi.pay({
    type: form.type,
    partner: form.partner,
    amount: amount.value,
    clearDocs: selected.value.map((x) => x.belnr),
  })
  load()
}
onMounted(load)
</script>
