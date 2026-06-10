# 电商购物平台 API 文档

**关联设计文档**：[电商购物平台设计](../02-design-docs/online-shop-platform-design.md)  
**文档版本**：v1.3  
**创建时间**：2026-06-09  
**最后更新**：2026-06-10  
**负责人**：@dev

---

## 概述

- **基础路径**：`/api`
- **认证方式**：商品接口公开访问；登录后端返回 Bearer Token，购物车和订单接口必须携带 `Authorization: Bearer <token>`
- **内容类型**：`application/json`
- **统一响应**：`{ "code": 200, "message": "success", "data": ... }`

---

## 商品接口

| 方法 | 路径 | 描述 | 请求参数 |
|------|------|------|----------|
| GET | `/api/products` | 商品分页列表和搜索 | `name`、`category`、`minPrice`、`maxPrice`、`page`、`pageSize` |
| GET | `/api/products/{id}` | 商品详情 | path: `id` |

**商品响应字段**：`id`、`name`、`category`、`price`、`stock`、`imageUrl`、`description`

**商品分页响应字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `items` | Array<Product> | 当前页商品列表 |
| `total` | Number | 符合筛选条件的商品总数 |
| `page` | Number | 当前页码，从 1 开始 |
| `pageSize` | Number | 每页商品数，取值 1-50 |
| `totalPages` | Number | 总页数 |

**商品分页请求示例**：

```http
GET /api/products?category=数码配件&page=1&pageSize=6
```

---

## 用户认证接口

| 方法 | 路径 | 描述 | 请求体 |
|------|------|------|--------|
| POST | `/api/auth/login` | 用户登录并签发令牌 | `{ "username": "demo", "password": "demo123" }` |

**登录响应字段**：`token`、`userId`、`username`、`nickname`

**调用受保护接口示例**：

```http
Authorization: Bearer eyJ...
```

---

## 购物车接口

| 方法 | 路径 | 描述 | 请求体 |
|------|------|------|--------|
| GET | `/api/cart` | 查询购物车 | — |
| POST | `/api/cart/items` | 加入购物车 | `{ "productId": 1, "quantity": 2 }` |
| PUT | `/api/cart/items/{id}` | 修改购物车数量 | `{ "quantity": 3 }` |
| DELETE | `/api/cart/items/{id}` | 删除购物车明细 | — |

**购物车响应字段**：`items`、`totalQuantity`、`totalAmount`

**购物车明细字段**：`id`、`productId`、`productName`、`category`、`price`、`quantity`、`stock`、`imageUrl`、`subtotal`

> 购物车接口均需要登录令牌。

---

## 订单接口

| 方法 | 路径 | 描述 | 请求体 |
|------|------|------|--------|
| POST | `/api/orders` | 提交订单 | `{ "receiverName": "张三", "receiverPhone": "13800000000", "receiverAddress": "上海市" }` |
| GET | `/api/orders` | 查询订单列表 | — |
| GET | `/api/orders/{id}` | 查询订单详情 | — |
| PUT | `/api/orders/{id}` | 更新订单收货信息 | `{ "receiverName": "李四", "receiverPhone": "13900000000", "receiverAddress": "北京市" }` |
| PUT | `/api/orders/{id}/cancel` | 取消订单 | — |
| DELETE | `/api/orders/{id}` | 删除已取消订单 | — |

**订单状态**：

| 状态 | 说明 |
|------|------|
| `CREATED` | 已创建 |
| `CANCELLED` | 已取消 |

**订单详情字段**：`id`、`orderNo`、`totalAmount`、`status`、`receiverName`、`receiverPhone`、`receiverAddress`、`createdAt`、`items`

> 订单接口均需要登录令牌，查询结果只返回当前登录用户的数据。仅 `CREATED` 状态订单允许修改或取消，仅 `CANCELLED` 状态订单允许删除。

---

## 错误码

| 业务码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误或业务校验失败 |
| 401 | 未登录、令牌无效或令牌过期 |
| 404 | 商品、购物车明细或订单不存在 |
| 500 | 服务器内部错误 |

---

## 变更记录

| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|----------|--------|
| v1.3 | 2026-06-10 | 商品列表接口新增 `page`、`pageSize` 分页参数，并返回 `items`、`total`、`page`、`pageSize`、`totalPages` 分页结构 | @dev |
| v1.2 | 2026-06-09 | 订单接口补齐 CRUD：新增更新订单和删除已取消订单接口；后端升级 Spring Boot 3 + MyBatis Plus | @dev |
| v1.1 | 2026-06-09 | 新增用户登录、Bearer Token 认证和受保护接口说明 | @dev |
| v1.0 | 2026-06-09 | 新增商品、购物车、订单核心接口 | @dev |
