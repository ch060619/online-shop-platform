# 电商购物平台前端

## 启动

```powershell
npm install
npm run dev
```

开发服务默认运行在 `http://localhost:5173`，并通过 Vite 代理访问后端 `/api`。

## 商品详情促销展示

商品详情页会在父组件 `src/views/ProductDetail.vue` 中组装商品促销信息，并通过 props 传递给两个子组件：

- `src/components/PromotionTags.vue`：展示促销标签。
- `src/components/PromotionCountdown.vue`：展示限时促销倒计时。

## 购物车状态

前端使用 Pinia 管理购物车全局状态，核心实现位于 `src/stores/cart.js`：

- 商品列表页和商品详情页调用 store 添加商品。
- 购物车页通过 store 加载、修改、删除和清空购物车。
- 结算页复用 store 中的购物车明细和总价，并在下单成功后跳转到 `/order-success/:id`。

未知路由会跳转到 `/404` 页面，并提供返回商品列表的入口。

## 构建

```powershell
npm run build
```
