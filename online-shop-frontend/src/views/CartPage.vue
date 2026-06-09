<template>
  <section class="page">
    <div class="section-title">
      <h1>购物车</h1>
      <el-button type="primary" :disabled="!cart.items.length" @click="$router.push('/checkout')">去结算</el-button>
    </div>
    <el-table :data="cart.items" empty-text="购物车为空">
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
          <el-input-number v-model="row.quantity" :min="1" :max="row.stock" size="small" @change="updateItem(row)" />
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
      <span>共 {{ cart.totalQuantity }} 件</span>
      <strong>合计 ￥{{ cart.totalAmount }}</strong>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { cartApi } from '../api'

const cart = reactive({ items: [], totalQuantity: 0, totalAmount: 0 })

const assignCart = (data) => {
  cart.items = data.items || []
  cart.totalQuantity = data.totalQuantity || 0
  cart.totalAmount = data.totalAmount || 0
}

const loadCart = async () => assignCart(await cartApi.get())
const updateItem = async (row) => assignCart(await cartApi.update(row.id, { quantity: row.quantity }))
const removeItem = async (id) => {
  assignCart(await cartApi.remove(id))
  ElMessage.success('已删除')
}

onMounted(loadCart)
</script>
