<template>
  <section class="page">
    <div class="page-title">
      <div><div class="eyebrow">{{ tcode }}</div><h2>{{ title }}</h2></div>
      <div class="page-actions"><slot name="actions" /></div>
    </div>
    <slot />
  </section>
</template>
<script setup>
defineProps({ title: String, tcode: String })
</script>
