<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <router-link class="brand" to="/products">电商购物平台</router-link>
      <nav class="nav">
        <router-link to="/products">商品</router-link>
        <router-link to="/cart">购物车</router-link>
        <router-link to="/orders">订单</router-link>
      </nav>
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>
