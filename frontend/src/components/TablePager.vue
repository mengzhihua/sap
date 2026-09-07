<template>
  <el-pagination
    v-if="total > size"
    class="pager"
    background
    layout="total, sizes, prev, pager, next"
    :total="total"
    :page-size="size"
    :current-page="page"
    :page-sizes="[10, 20, 50]"
    @current-change="$emit('change', $event)"
    @size-change="$emit('size', $event)"
  />
</template>
<script setup>
defineProps({ total: Number, page: Number, size: Number })
defineEmits(['change', 'size'])
</script>
