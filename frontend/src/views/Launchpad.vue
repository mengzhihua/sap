<template>
  <PageShell title="Launchpad" tcode="SAP">
    <div v-for="group in groups" :key="group.name" class="launch-group">
      <h3>{{ group.name }}</h3>
      <el-row :gutter="16">
        <el-col v-for="item in group.children" :key="`${item[0]}-${item[2]}`" :xs="12" :sm="8" :md="6" :lg="4">
          <el-card class="launch-tile" shadow="hover" @click="$router.push(item[2])">
            <div class="tile-code">{{ item[0] }}</div><div class="tile-name">{{ item[1] }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </PageShell>
</template>
<script setup>
import PageShell from '../components/PageShell.vue'
import { groups } from '../router'
</script>
