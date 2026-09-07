<template>
  <div class="login-page">
    <el-card class="login-card">
      <h1>SAP 复刻系统</h1>
      <p>企业资源计划 Launchpad</p>
      <el-form
        :model="form"
        @submit.prevent="submit"
      >
        <el-form-item
          ><el-input
            v-model="form.username"
            placeholder="用户名"
        /></el-form-item>
        <el-form-item
          ><el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="密码"
            @keyup.enter="submit"
        /></el-form-item>
        <el-button
          type="primary"
          class="wide"
          :loading="loading"
          @click="submit"
          >登录</el-button
        >
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'
import { setAuth } from '../auth'
const form = reactive({ username: 'admin', password: 'admin123' })
const loading = ref(false)
const router = useRouter()
const route = useRoute()
async function submit() {
  loading.value = true
  try {
    const data = await authApi.login(form)
    setAuth(data.token, data.user)
    router.replace(route.query.redirect || '/launchpad')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
