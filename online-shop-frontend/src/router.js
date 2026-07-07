import { createRouter, createWebHistory } from 'vue-router'
import ProductList from './views/ProductList.vue'
import ProductDetail from './views/ProductDetail.vue'
import CartPage from './views/CartPage.vue'
import CheckoutPage from './views/CheckoutPage.vue'
import OrderList from './views/OrderList.vue'
import OrderDetail from './views/OrderDetail.vue'
import LoginPage from './views/LoginPage.vue'
import OrderSuccess from './views/OrderSuccess.vue'
import ProfilePage from './views/ProfilePage.vue'
import NotFound from './views/NotFound.vue'
import { isAuthenticated } from './auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/products' },
    { path: '/login', component: LoginPage },
    { path: '/products', component: ProductList },
    { path: '/products/:id', component: ProductDetail },
    { path: '/cart', component: CartPage, meta: { requiresAuth: true } },
    { path: '/checkout', component: CheckoutPage, meta: { requiresAuth: true } },
    { path: '/order-success/:id', component: OrderSuccess, meta: { requiresAuth: true } },
    { path: '/orders', component: OrderList, meta: { requiresAuth: true } },
    { path: '/orders/:id', component: OrderDetail, meta: { requiresAuth: true } },
    { path: '/profile', component: ProfilePage, meta: { requiresAuth: true } },
    { path: '/404', component: NotFound },
    { path: '/:pathMatch(.*)*', redirect: '/404' }
  ]
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !isAuthenticated()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && isAuthenticated()) {
    return to.query.redirect || '/products'
  }
  return true
})

export default router
