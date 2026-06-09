<template>
  <section class="page">
    <div class="section-title">
      <h1>订单列表</h1>
      <el-button @click="loadOrders">刷新</el-button>
    </div>
    <el-table :data="orders" empty-text="暂无订单">
      <el-table-column prop="orderNo" label="订单编号" min-width="220" />
      <el-table-column prop="totalAmount" label="金额" width="120" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="createdAt" label="创建时间" width="190" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/orders/${row.id}`)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { orderApi } from '../api'

const orders = ref([])
const loadOrders = async () => {
  orders.value = await orderApi.list()
}

onMounted(loadOrders)
</script>
