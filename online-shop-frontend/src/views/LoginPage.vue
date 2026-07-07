<template>
  <section class="login-page">
    <el-form class="login-panel" :model="form" label-position="top" @keyup.enter="submit">
      <h1>{{ mode === 'login' ? '用户登录' : '用户注册' }}</h1>
      <el-segmented v-model="mode" :options="modeOptions" class="auth-mode" />
      <el-form-item label="用户名">
        <el-input v-model="form.username" autocomplete="username" />
      </el-form-item>
      <template v-if="mode === 'register'">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
      </template>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
      </el-form-item>
      <el-button type="primary" :loading="loading" @click="submit">{{ mode === 'login' ? '登录' : '注册' }}</el-button>
      <p class="muted" v-if="mode === 'login'">演示账号：demo / demo123</p>
      <p class="muted" v-else>注册后会自动登录，密码使用服务端 BCrypt 保存</p>
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
const mode = ref('login')
const modeOptions = [
  { label: '登录', value: 'login' },
  { label: '注册', value: 'register' }
]
const form = reactive({
  username: 'demo',
  password: 'demo123',
  nickname: '',
  phone: ''
})

const mobilePattern = /^1[3-9]\d{9}$/

const validateForm = () => {
  if (!form.username.trim()) {
    ElMessage.error('用户名不能为空')
    return false
  }
  if (!form.password) {
    ElMessage.error('密码不能为空')
    return false
  }
  if (mode.value === 'register' && !form.nickname.trim()) {
    ElMessage.error('昵称不能为空')
    return false
  }
  if (mode.value === 'register' && form.phone && !mobilePattern.test(form.phone)) {
    ElMessage.error('手机号格式不正确')
    return false
  }
  return true
}

const submit = async () => {
  if (!validateForm()) {
    return
  }
  loading.value = true
  try {
    const payload = mode.value === 'login'
      ? { username: form.username, password: form.password }
      : {
          username: form.username,
          password: form.password,
          nickname: form.nickname,
          phone: form.phone
        }
    const result = mode.value === 'login' ? await authApi.login(payload) : await authApi.register(payload)
    setAuth(result)
    ElMessage.success(mode.value === 'login' ? '登录成功' : '注册成功')
    router.push(route.query.redirect || '/products')
  } finally {
    loading.value = false
  }
}
</script>
