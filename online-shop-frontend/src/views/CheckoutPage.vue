<template>
  <section class="page checkout-grid">
    <div>
      <h1>订单确认</h1>
      <el-table :data="cart.items" empty-text="购物车为空">
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="price" label="单价" width="120" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="subtotal" label="小计" width="120" />
      </el-table>
      <div class="summary-bar">
        <span>共 {{ cart.totalQuantity }} 件</span>
        <strong>应付 ￥{{ cart.totalAmount }}</strong>
      </div>
    </div>
    <el-form class="checkout-form" :model="form" label-position="top">
      <el-form-item label="收货人">
        <el-input v-model="form.receiverName" />
      </el-form-item>
      <el-form-item label="联系方式">
        <el-input v-model="form.receiverPhone" />
      </el-form-item>
      <el-form-item label="收货地址">
        <el-input v-model="form.receiverAddress" type="textarea" :rows="4" />
      </el-form-item>
      <el-button type="primary" :disabled="!cart.items.length" @click="submitOrder">提交订单</el-button>
    </el-form>
  </section>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cartApi, orderApi } from '../api'

const router = useRouter()
const cart = reactive({ items: [], totalQuantity: 0, totalAmount: 0 })
const form = reactive({
  receiverName: '演示用户',
  receiverPhone: '13800000000',
  receiverAddress: '上海市浦东新区世纪大道 1 号'
})

const loadCart = async () => {
  const data = await cartApi.get()
  cart.items = data.items || []
  cart.totalQuantity = data.totalQuantity || 0
  cart.totalAmount = data.totalAmount || 0
}

const submitOrder = async () => {
  const order = await orderApi.create(form)
  ElMessage.success('订单提交成功')
  router.push(`/orders/${order.id}`)
}

onMounted(loadCart)
</script>
