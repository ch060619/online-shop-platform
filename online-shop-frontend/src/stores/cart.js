import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { cartApi } from '../api'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)
  const serverTotals = ref({
    totalQuantity: 0,
    totalAmount: 0,
    selectedQuantity: 0,
    selectedAmount: 0
  })

  const computedTotalQuantity = computed(() => items.value.reduce(
    (sum, item) => sum + Number(item.quantity || 0),
    0
  ))
  const computedTotalAmount = computed(() => items.value.reduce(
    (sum, item) => sum + Number(item.subtotal || Number(item.price || 0) * Number(item.quantity || 0)),
    0
  ))
  const totalQuantity = computed(() => Number(serverTotals.value.totalQuantity || computedTotalQuantity.value))
  const totalAmount = computed(() => Number(serverTotals.value.totalAmount || computedTotalAmount.value))
  const selectedItems = computed(() => items.value.filter((item) => item.selected !== false))
  const selectedQuantity = computed(() => Number(serverTotals.value.selectedQuantity || selectedItems.value.reduce(
    (sum, item) => sum + Number(item.quantity || 0),
    0
  )))
  const selectedAmount = computed(() => Number(serverTotals.value.selectedAmount || selectedItems.value.reduce(
    (sum, item) => sum + Number(item.subtotal || Number(item.price || 0) * Number(item.quantity || 0)),
    0
  )))
  const formattedTotalAmount = computed(() => totalAmount.value.toFixed(2))
  const formattedSelectedAmount = computed(() => selectedAmount.value.toFixed(2))

  const assignCart = (cart = {}) => {
    items.value = cart.items || []
    serverTotals.value = {
      totalQuantity: Number(cart.totalQuantity || 0),
      totalAmount: Number(cart.totalAmount || 0),
      selectedQuantity: Number(cart.selectedQuantity || 0),
      selectedAmount: Number(cart.selectedAmount || 0)
    }
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

  const updateSelected = async (item, selected) => {
    assignCart(await cartApi.update(item.id, { quantity: item.quantity, selected }))
  }

  const removeItem = async (id) => {
    assignCart(await cartApi.remove(id))
  }

  const clearCart = async () => {
    for (const item of [...items.value]) {
      await cartApi.remove(item.id)
    }
    items.value = []
    serverTotals.value = {
      totalQuantity: 0,
      totalAmount: 0,
      selectedQuantity: 0,
      selectedAmount: 0
    }
  }

  return {
    items,
    loading,
    totalQuantity,
    totalAmount,
    selectedItems,
    selectedQuantity,
    selectedAmount,
    formattedTotalAmount,
    formattedSelectedAmount,
    loadCart,
    addItem,
    updateItem,
    updateSelected,
    removeItem,
    clearCart
  }
})
