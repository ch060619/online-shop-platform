import { createRouter, createWebHistory } from 'vue-router'
import ProductList from './views/ProductList.vue'
import ProductDetail from './views/ProductDetail.vue'
import CartPage from './views/CartPage.vue'
import CheckoutPage from './views/CheckoutPage.vue'
import OrderList from './views/OrderList.vue'
import OrderDetail from './views/OrderDetail.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/products' },
    { path: '/products', component: ProductList },
    { path: '/products/:id', component: ProductDetail },
    { path: '/cart', component: CartPage },
    { path: '/checkout', component: CheckoutPage },
    { path: '/orders', component: OrderList },
    { path: '/orders/:id', component: OrderDetail }
  ]
})

export default router
