<template>
  <section class="page detail-page" v-if="product">
    <img class="detail-image" :src="product.imageUrl" :alt="product.name" />
    <div class="detail-info">
      <el-tag>{{ product.category }}</el-tag>
      <h1>{{ product.name }}</h1>
      <p>{{ product.description }}</p>
      <div class="price-line">￥{{ product.price }}</div>
      <div class="stock-line">库存 {{ product.stock }}</div>
      <div class="buy-row">
        <el-input-number v-model="quantity" :min="1" :max="product.stock" />
        <el-button type="primary" :disabled="product.stock <= 0" @click="addToCart">加入购物车</el-button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cartApi, productApi } from '../api'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const quantity = ref(1)

const loadProduct = async () => {
  product.value = await productApi.detail(route.params.id)
}

const addToCart = async () => {
  await cartApi.add({ productId: product.value.id, quantity: quantity.value })
  ElMessage.success('已加入购物车')
  router.push('/cart')
}

onMounted(loadProduct)
</script>
