<template>
  <el-drawer
    v-model="open"
    :title="title"
    size="62%"
    @closed="$emit('update:modelValue', false)"
  >
    <pre>{{ pretty }}</pre>
  </el-drawer>
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ modelValue: Boolean, title: String, value: [Object, String] })
const emit = defineEmits(['update:modelValue'])
const open = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) })
const pretty = computed(() => {
  if (typeof props.value !== 'string') return JSON.stringify(props.value || {}, null, 2)
  try {
    return JSON.stringify(JSON.parse(props.value), null, 2)
  } catch {
    return props.value || ''
  }
})
</script>
