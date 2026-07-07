<template>
  <section class="page" v-if="order">
    <div class="section-title">
      <div>
        <h1>订单详情</h1>
        <p class="muted">{{ order.orderNo }} · {{ order.status }}</p>
      </div>
      <div class="button-row">
        <el-button type="primary" :disabled="order.status !== 'CREATED'" @click="payOrder">支付</el-button>
        <el-button type="danger" :disabled="order.status !== 'CREATED'" @click="cancelOrder">取消订单</el-button>
      </div>
    </div>
    <div class="order-address">
      <strong>{{ order.receiverName }}</strong>
      <span>{{ order.receiverPhone }}</span>
      <p>{{ order.receiverAddress }}</p>
    </div>
    <el-table :data="order.items">
      <el-table-column label="商品" min-width="260">
        <template #default="{ row }">
          <div class="table-product">
            <img :src="row.productImageUrl" :alt="row.productName" />
            <span>{{ row.productName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="成交价" width="120" />
      <el-table-column prop="quantity" label="数量" width="100" />
      <el-table-column prop="subtotal" label="小计" width="120" />
    </el-table>
    <div class="summary-bar">
      <span>创建时间 {{ order.createdAt }}</span>
      <strong>实付 ￥{{ order.totalAmount }}</strong>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderApi } from '../api'

const route = useRoute()
const router = useRouter()
const order = ref(null)

const loadOrder = async () => {
  order.value = await orderApi.detail(route.params.id)
}

const cancelOrder = async () => {
  order.value = await orderApi.cancel(route.params.id)
  ElMessage.success('订单已取消')
}

const payOrder = async () => {
  order.value = await orderApi.pay(route.params.id)
  ElMessage.success('支付完成')
  router.push(`/order-success/${route.params.id}?paid=1`)
}

onMounted(loadOrder)
</script>
