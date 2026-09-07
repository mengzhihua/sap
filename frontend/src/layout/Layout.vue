<template>
  <el-container class="shell">
    <el-header class="topbar">
      <div class="brand">SAP 复刻系统</div>
      <el-autocomplete v-model="command" class="command" :fetch-suggestions="suggest" placeholder="输入事务代码，如 MM01" @select="goTcode" @keyup.enter="lookup" />
      <div class="user"><span>{{ auth.user?.realName || auth.user?.username }}</span><el-button link @click="logout">退出</el-button></div>
    </el-header>
    <el-container>
      <el-aside width="236px" class="sidebar">
        <el-menu :default-active="$route.path" router background-color="#1f3548" text-color="#d9e2ec" active-text-color="#fff">
          <el-menu-item index="/dashboard">⌂　Launchpad 工作台</el-menu-item>
          <el-sub-menu v-for="group in groups" :key="group.name" :index="group.name">
            <template #title>{{ group.name }}</template>
            <el-menu-item v-for="item in group.children" :key="item[2]" :index="item[2]">{{ item[0] }}　{{ item[1] }}</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-main class="content">
        <div class="crumb">{{ $route.meta.tcode }} – {{ $route.meta.title || '工作台' }}</div>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { groups } from '../router'
import { api } from '../api'
import { auth, clearAuth } from '../auth'
const router = useRouter()
const command = ref('')
function suggest(query, cb) {
  api.tcodes(query).then((rows) => cb((rows || []).map((x) => ({ value: `${x.tcode} – ${x.name}`, item: x })))).catch(() => cb([]))
}
function goTcode(item) { router.push(item.item.route); command.value = '' }
async function lookup() {
  const q = command.value.trim().split(/\s+/)[0]
  if (!q) return
  const rows = await api.tcodes(q)
  const item = (rows || []).find((x) => x.tcode === q)
  if (!item) ElMessage.error('事务代码不存在')
  else router.push(item.route)
  command.value = ''
}
function logout() { clearAuth(); router.replace('/login') }
</script>
