<template>
  <section class="page">
    <div class="section-title">
      <h1>购物车</h1>
      <div class="button-row">
        <el-button :disabled="!cartStore.items.length" @click="clearCart">清空购物车</el-button>
        <el-button type="primary" :disabled="!cartStore.items.length" @click="$router.push('/checkout')">去结算</el-button>
      </div>
    </div>
    <el-table v-loading="cartStore.loading" :data="cartStore.items" empty-text="购物车为空">
      <el-table-column label="商品" min-width="260">
        <template #default="{ row }">
          <div class="table-product">
            <img :src="row.imageUrl" :alt="row.productName" />
            <div>
              <router-link :to="`/products/${row.productId}`">{{ row.productName }}</router-link>
              <p>{{ row.category }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="单价" width="120" />
      <el-table-column label="数量" width="170">
        <template #default="{ row }">
          <el-input-number :model-value="row.quantity" :min="1" :max="row.stock" size="small" @change="(quantity) => updateItem(row.id, quantity)" />
        </template>
      </el-table-column>
      <el-table-column prop="subtotal" label="小计" width="120" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" link @click="removeItem(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="summary-bar">
      <span>共 {{ cartStore.totalQuantity }} 件</span>
      <strong>合计 ￥{{ cartStore.formattedTotalAmount }}</strong>
    </div>
  </section>
</template>

<script setup>
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useCartStore } from '../stores/cart'

const cartStore = useCartStore()

const updateItem = async (id, quantity) => {
  await cartStore.updateItem(id, quantity)
}

const removeItem = async (id) => {
  await cartStore.removeItem(id)
  ElMessage.success('已删除')
}

const clearCart = async () => {
  await cartStore.clearCart()
  ElMessage.success('购物车已清空')
}

onMounted(cartStore.loadCart)
</script>
