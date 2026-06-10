import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { cartApi } from '../api'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)

  const totalQuantity = computed(() => items.value.reduce(
    (sum, item) => sum + Number(item.quantity || 0),
    0
  ))
  const totalAmount = computed(() => items.value.reduce(
    (sum, item) => sum + Number(item.subtotal || Number(item.price || 0) * Number(item.quantity || 0)),
    0
  ))
  const formattedTotalAmount = computed(() => totalAmount.value.toFixed(2))

  const assignCart = (cart = {}) => {
    items.value = cart.items || []
  }

  const loadCart = async () => {
    loading.value = true
    try {
      assignCart(await cartApi.get())
    } finally {
      loading.value = false
    }
  }

  const addItem = async (productId, quantity = 1) => {
    assignCart(await cartApi.add({ productId, quantity }))
  }

  const updateItem = async (id, quantity) => {
    assignCart(await cartApi.update(id, { quantity }))
  }

  const removeItem = async (id) => {
    assignCart(await cartApi.remove(id))
  }

  const clearCart = async () => {
    for (const item of [...items.value]) {
      await cartApi.remove(item.id)
    }
    items.value = []
  }

  return {
    items,
    loading,
    totalQuantity,
    totalAmount,
    formattedTotalAmount,
    loadCart,
    addItem,
    updateItem,
    removeItem,
    clearCart
  }
})
