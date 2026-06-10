<template>
  <section class="page">
    <div class="toolbar">
      <el-input v-model="filters.name" clearable placeholder="商品名称" />
      <el-select v-model="filters.category" clearable placeholder="分类" @change="searchProducts" @clear="searchProducts">
        <el-option v-for="category in categories" :key="category" :label="category" :value="category" />
      </el-select>
      <el-input-number v-model="filters.minPrice" :min="0" placeholder="最低价" />
      <el-input-number v-model="filters.maxPrice" :min="0" placeholder="最高价" />
      <el-button type="primary" @click="searchProducts">搜索</el-button>
    </div>

    <el-row v-loading="loading" :gutter="16">
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

    <el-empty v-if="!loading && products.length === 0" description="暂无商品" />

    <div class="pagination-bar">
      <span>共 {{ pagination.total }} 件商品</span>
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[3, 6, 9]"
        :total="pagination.total"
        background
        layout="sizes, prev, pager, next"
        @current-change="loadProducts"
        @size-change="handlePageSizeChange"
      />
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productApi } from '../api'
import { isAuthenticated } from '../auth'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const products = ref([])
const loading = ref(false)
const categories = ['数码配件', '运动户外', '生活家居', '服饰鞋包']
const filters = reactive({
  name: '',
  category: '',
  minPrice: undefined,
  maxPrice: undefined
})
const pagination = reactive({
  page: 1,
  pageSize: 6,
  total: 0
})

const normalizePage = (response) => {
  if (Array.isArray(response)) {
    return {
      items: response,
      total: response.length,
      page: pagination.page,
      pageSize: pagination.pageSize
    }
  }
  return response
}

const loadProducts = async (page = pagination.page) => {
  loading.value = true
  pagination.page = page
  try {
    const result = normalizePage(await productApi.list({
      ...filters,
      page: pagination.page,
      pageSize: pagination.pageSize
    }))
    products.value = result.items || []
    pagination.total = result.total || 0
    pagination.page = result.page || pagination.page
    pagination.pageSize = result.pageSize || pagination.pageSize
  } finally {
    loading.value = false
  }
}

const searchProducts = () => {
  loadProducts(1)
}

const handlePageSizeChange = () => {
  loadProducts(1)
}

const addToCart = async (productId) => {
  if (!isAuthenticated()) {
    router.push({ path: '/login', query: { redirect: '/products' } })
    return
  }
  await cartStore.addItem(productId, 1)
  ElMessage.success('已加入购物车')
}

onMounted(loadProducts)
</script>
