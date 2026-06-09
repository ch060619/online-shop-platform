<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <router-link class="brand" to="/products">电商购物平台</router-link>
      <nav class="nav">
        <router-link to="/products">商品</router-link>
        <router-link to="/cart">购物车</router-link>
        <router-link to="/orders">订单</router-link>
      </nav>
      <div class="user-area">
        <template v-if="authState.user">
          <span>{{ authState.user.nickname || authState.user.username }}</span>
          <el-button link type="primary" @click="handleLogout">退出</el-button>
        </template>
        <el-button v-else type="primary" @click="$router.push('/login')">登录</el-button>
      </div>
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { authState, clearAuth } from './auth'

const router = useRouter()

const handleLogout = () => {
  clearAuth()
  router.push('/products')
}
</script>
