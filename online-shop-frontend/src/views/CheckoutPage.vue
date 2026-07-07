<template>
  <section class="page checkout-grid">
    <div>
      <h1>订单确认</h1>
      <el-table v-loading="cartStore.loading" :data="cartStore.selectedItems" empty-text="请选择要结算的商品">
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="price" label="单价" width="120" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="subtotal" label="小计" width="120" />
      </el-table>
      <div class="summary-bar">
        <span>已选 {{ cartStore.selectedQuantity }} 件</span>
        <strong>应付 ￥{{ cartStore.formattedSelectedAmount }}</strong>
      </div>
    </div>
    <el-form class="checkout-form" :model="form" label-position="top">
      <el-form-item label="选择地址">
        <el-select v-model="selectedAddressId" placeholder="选择收货地址" clearable @change="fillAddress">
          <el-option
            v-for="address in addresses"
            :key="address.id"
            :label="`${address.receiverName} ${address.receiverPhone}`"
            :value="address.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="收货人">
        <el-input v-model="form.receiverName" />
      </el-form-item>
      <el-form-item label="联系方式">
        <el-input v-model="form.receiverPhone" />
      </el-form-item>
      <el-form-item label="收货地址">
        <el-input v-model="form.receiverAddress" type="textarea" :rows="4" />
      </el-form-item>
      <el-button type="primary" :disabled="!cartStore.selectedItems.length" @click="submitOrder">提交订单</el-button>
    </el-form>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addressApi, orderApi } from '../api'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const addresses = ref([])
const selectedAddressId = ref(null)
const form = reactive({
  receiverName: '演示用户',
  receiverPhone: '13800000000',
  receiverAddress: '上海市浦东新区世纪大道 1 号'
})

const loadAddresses = async () => {
  addresses.value = await addressApi.list()
  const defaultAddress = addresses.value.find((address) => address.defaultAddress) || addresses.value[0]
  if (defaultAddress) {
    selectedAddressId.value = defaultAddress.id
    applyAddress(defaultAddress)
  }
}

const fillAddress = (id) => {
  const address = addresses.value.find((item) => item.id === id)
  if (address) {
    applyAddress(address)
  }
}

const applyAddress = (address) => {
  form.receiverName = address.receiverName
  form.receiverPhone = address.receiverPhone
  form.receiverAddress = address.receiverAddress
}

const submitOrder = async () => {
  const order = await orderApi.create(form)
  await cartStore.loadCart()
  ElMessage.success('订单提交成功')
  router.push(`/order-success/${order.id}`)
}

onMounted(async () => {
  await Promise.all([cartStore.loadCart(), loadAddresses()])
})
</script>
