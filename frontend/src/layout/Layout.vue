<template>
  <el-container class="shell">
    <el-header class="topbar">
      <div class="brand">SAP ERP</div>
      <el-autocomplete
        v-model="command"
        class="command"
        :fetch-suggestions="suggest"
        placeholder="输入事务代码，如 MM01"
        @select="goTcode"
        @keyup.enter="lookup"
      />
      <el-dropdown
        class="user"
        @command="onUserCommand"
      >
        <span class="user-link"
          >{{ auth.user?.realName || auth.user?.username }} <el-icon><ArrowDown /></el-icon
        ></span>
        <template #dropdown
          ><el-dropdown-menu
            ><el-dropdown-item command="logout">退出登录</el-dropdown-item></el-dropdown-menu
          ></template
        >
      </el-dropdown>
    </el-header>
    <el-container>
      <el-aside
        width="236px"
        class="sidebar"
      >
        <el-menu
          :default-active="$route.path"
          router
          background-color="#1f3548"
          text-color="#d9e2ec"
          active-text-color="#fff"
        >
          <el-menu-item index="/launchpad">⌂　Launchpad 工作台</el-menu-item>
          <el-sub-menu
            v-for="group in groups"
            :key="group.name"
            :index="group.name"
          >
            <template #title>{{ group.name }}</template>
            <el-menu-item
              v-for="item in group.children"
              :key="item[2]"
              :index="item[2]"
              >{{ item[0] }}　{{ item[1] }}</el-menu-item
            >
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
import { ArrowDown } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { groups } from '../router/groups'
import { basisApi } from '../api/basis'
import { auth, clearAuth } from '../auth'
const router = useRouter()
const command = ref('')
function suggest(query, cb) {
  basisApi
    .tcodes(query)
    .then((rows) => cb((rows || []).map((x) => ({ value: `${x.tcode} – ${x.name}`, item: x }))))
    .catch(() => cb([]))
}
function goTcode(item) {
  router.push(item.item.route)
  command.value = ''
}
async function lookup() {
  const q = command.value.trim().split(/\s+/)[0]
  if (!q) return
  const rows = await basisApi.tcodes(q)
  const item = (rows || []).find((x) => x.tcode === q)
  if (!item) ElMessage.warning('事务代码不存在')
  else router.push(item.route)
  command.value = ''
}
function onUserCommand(value) {
  if (value === 'logout') {
    clearAuth()
    router.replace('/login')
  }
}
</script>
