<template>
  <section class="page result-page">
    <div class="result-panel">
      <el-result icon="success" :title="title" :sub-title="subTitle">
        <template #extra>
          <div class="button-row center">
            <el-button type="primary" @click="$router.push(`/orders/${orderId}`)">查看订单</el-button>
            <el-button @click="$router.push('/products')">继续购物</el-button>
          </div>
        </template>
      </el-result>
    </div>
  </section>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
const orderId = route.params.id
const paid = route.query.paid === '1'
const title = paid ? '支付完成' : '下单成功'
const subTitle = paid ? '订单已支付完成，可查看详情或继续购物。' : '订单已创建，可继续查看详情或返回商品列表。'
</script>
