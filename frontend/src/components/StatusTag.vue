<template><el-tag size="small" :type="type">{{ value || '-' }}</el-tag></template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ value: [String, Number] })
const type = computed(() => ({ OPEN: 'warning', PARTIAL: 'warning', CLOSED: 'info', POSTED: 'success', BLOCKED: 'danger', PGI: 'success', PICKED: 'success', SUCCESS: 'success', FAIL: 'danger', 1: 'success', 0: 'danger' }[props.value] || 'info'))
</script>
