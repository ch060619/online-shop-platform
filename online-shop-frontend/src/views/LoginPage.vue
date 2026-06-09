<template>
  <section class="login-page">
    <el-form class="login-panel" :model="form" label-position="top" @keyup.enter="submitLogin">
      <h1>用户登录</h1>
      <el-form-item label="用户名">
        <el-input v-model="form.username" autocomplete="username" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
      </el-form-item>
      <el-button type="primary" :loading="loading" @click="submitLogin">登录</el-button>
      <p class="muted">演示账号：demo / demo123</p>
    </el-form>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'
import { setAuth } from '../auth'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: 'demo',
  password: 'demo123'
})

const submitLogin = async () => {
  loading.value = true
  try {
    const result = await authApi.login(form)
    setAuth(result)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/products')
  } finally {
    loading.value = false
  }
}
</script>
