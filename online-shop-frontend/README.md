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

## 构建

```powershell
npm run build
```
