# 电商购物平台 API 文档

**关联设计文档**：[电商购物平台设计](../02-design-docs/online-shop-platform-design.md)  
**文档版本**：v1.0  
**创建时间**：2026-06-09  
**最后更新**：2026-06-09  
**负责人**：@dev

---

## 概述

- **基础路径**：`/api`
- **认证方式**：实验初版使用固定用户上下文，Shiro 已接入并预留登录校验扩展点
- **内容类型**：`application/json`
- **统一响应**：`{ "code": 200, "message": "success", "data": ... }`

---

## 商品接口

| 方法 | 路径 | 描述 | 请求参数 |
|------|------|------|----------|
| GET | `/api/products` | 商品列表和搜索 | `name`、`category`、`minPrice`、`maxPrice` |
| GET | `/api/products/{id}` | 商品详情 | path: `id` |

**商品响应字段**：`id`、`name`、`category`、`price`、`stock`、`imageUrl`、`description`

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

---

## 订单接口

| 方法 | 路径 | 描述 | 请求体 |
|------|------|------|--------|
| POST | `/api/orders` | 提交订单 | `{ "receiverName": "张三", "receiverPhone": "13800000000", "receiverAddress": "上海市" }` |
| GET | `/api/orders` | 查询订单列表 | — |
| GET | `/api/orders/{id}` | 查询订单详情 | — |
| PUT | `/api/orders/{id}/cancel` | 取消订单 | — |

**订单状态**：

| 状态 | 说明 |
|------|------|
| `CREATED` | 已创建 |
| `CANCELLED` | 已取消 |

**订单详情字段**：`id`、`orderNo`、`totalAmount`、`status`、`receiverName`、`receiverPhone`、`receiverAddress`、`createdAt`、`items`

---

## 错误码

| 业务码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误或业务校验失败 |
| 404 | 商品、购物车明细或订单不存在 |
| 500 | 服务器内部错误 |

---

## 变更记录

| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|----------|--------|
| v1.0 | 2026-06-09 | 新增商品、购物车、订单核心接口 | @dev |
