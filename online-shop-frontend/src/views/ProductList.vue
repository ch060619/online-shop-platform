<template>
  <section class="page">
    <div class="toolbar">
      <el-input v-model="filters.name" clearable placeholder="商品名称" />
      <el-select v-model="filters.category" clearable placeholder="分类">
        <el-option v-for="category in categories" :key="category" :label="category" :value="category" />
      </el-select>
      <el-input-number v-model="filters.minPrice" :min="0" placeholder="最低价" />
      <el-input-number v-model="filters.maxPrice" :min="0" placeholder="最高价" />
      <el-button type="primary" @click="loadProducts">搜索</el-button>
    </div>

    <el-row :gutter="16">
      <el-col v-for="product in products" :key="product.id" :xs="24" :sm="12" :lg="8">
        <article class="product-card">
          <img :src="product.imageUrl" :alt="product.name" />
          <div class="product-body">
            <div class="product-title">
              <router-link :to="`/products/${product.id}`">{{ product.name }}</router-link>
              <el-tag size="small">{{ product.category }}</el-tag>
            </div>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-meta">
              <strong>￥{{ product.price }}</strong>
              <span>库存 {{ product.stock }}</span>
            </div>
            <div class="actions">
              <el-button @click="$router.push(`/products/${product.id}`)">详情</el-button>
              <el-button type="primary" :disabled="product.stock <= 0" @click="addToCart(product.id)">加入购物车</el-button>
            </div>
          </div>
        </article>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { cartApi, productApi } from '../api'

const products = ref([])
const categories = ['数码配件', '运动户外', '生活家居', '服饰鞋包']
const filters = reactive({
  name: '',
  category: '',
  minPrice: undefined,
  maxPrice: undefined
})

const loadProducts = async () => {
  products.value = await productApi.list(filters)
}

const addToCart = async (productId) => {
  await cartApi.add({ productId, quantity: 1 })
  ElMessage.success('已加入购物车')
}

onMounted(loadProducts)
</script>
