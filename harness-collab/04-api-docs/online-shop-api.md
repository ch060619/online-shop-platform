# 电商购物平台 API 文档

**关联设计文档**：[电商购物平台设计](../02-design-docs/online-shop-platform-design.md)  
**文档版本**：v1.8
**创建时间**：2026-06-09  
**最后更新**：2026-06-15  
**负责人**：@dev

---

## 概述

- **基础路径**：`/api`
- **认证方式**：商品接口公开访问；登录后端返回 Bearer Token，购物车和订单接口必须携带 `Authorization: Bearer <token>`
- **内容类型**：`application/json`
- **统一响应**：`{ "code": 200, "message": "success", "data": ..., "page": null }`
- **分页响应**：分页接口会额外返回 `page` 元信息；为兼容既有前端，`data` 中仍保留分页对象。

---

## 商品接口

| 方法 | 路径 | 描述 | 请求参数 |
|------|------|------|----------|
| GET | `/api/products` | 商品分页列表和搜索 | `name`、`category`、`minPrice`、`maxPrice`、`page`、`pageSize` |
| GET | `/api/products/{id}` | 商品详情 | path: `id` |
| POST | `/api/products/add` | 新增商品 | JSON 请求体 |
| DELETE | `/api/products/delete/{id}` | 删除商品 | path: `id` |
| PUT | `/api/products/update/{id}` | 更新商品 | path: `id` + JSON 请求体 |
| GET | `/api/products/query` | 商品分页查询兼容入口 | `name`、`category`、`minPrice`、`maxPrice`、`page`、`pageSize` |

**商品响应字段**：`id`、`name`、`category`、`price`、`stock`、`imageUrl`、`description`

**商品新增/更新请求体**：

```json
{
  "name": "机械键盘",
  "category": "数码配件",
  "price": 299.00,
  "stock": 10,
  "imageUrl": "image",
  "description": "desc"
}
```

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
| POST | `/api/orders` | 幂等提交订单 | Header: `Idempotency-Key` + `{ "receiverName": "张三", "receiverPhone": "13800000000", "receiverAddress": "上海市" }` |
| GET | `/api/orders` | 查询订单列表 | — |
| GET | `/api/orders/{id}` | 查询订单详情 | — |
| PUT | `/api/orders/{id}` | 更新订单收货信息 | `{ "receiverName": "李四", "receiverPhone": "13900000000", "receiverAddress": "北京市" }` |
| PUT | `/api/orders/{id}/pay` | 模拟支付订单 | — |
| PUT | `/api/orders/{id}/cancel` | 取消订单 | — |
| DELETE | `/api/orders/{id}` | 删除已取消订单 | — |

**订单状态**：

| 状态 | 说明 |
|------|------|
| `CREATED` | 已创建 |
| `PAID` | 已支付 |
| `CANCELLED` | 已取消 |
| `TIMEOUT` | 超时关闭 |

**订单详情字段**：`id`、`orderNo`、`totalAmount`、`status`、`receiverName`、`receiverPhone`、`receiverAddress`、`createdAt`、`expireAt`、`paidAt`、`items`

`createdAt`、`expireAt`、`paidAt` 由后端应用使用系统时间写入，持久化为 `yyyy-MM-dd HH:mm:ss` 格式；读取时兼容历史 ISO `yyyy-MM-ddTHH:mm:ss` 格式，避免 SQLite 时间戳解析失败。

**订单提交幂等规则**：

- `POST /api/orders` 必须携带 `Idempotency-Key` 请求头，同一次客户端提交重试必须使用同一个值。
- 同一用户、同一 `Idempotency-Key` 且请求体一致时，重复请求返回同一笔订单，不重复扣减库存。
- 同一用户、同一 `Idempotency-Key` 但请求体不一致时，返回 `code=409`。
- 首次请求仍在处理中时，重复请求返回 `code=409`，提示稍后重试。

> 订单接口均需要登录令牌，查询结果只返回当前登录用户的数据。仅 `CREATED` 状态订单允许修改或取消，仅 `CANCELLED` 状态订单允许删除。

**订单状态机规则**：

- `CREATED -> PAID`：调用 `PUT /api/orders/{id}/pay` 模拟支付成功，记录 `paidAt`。
- `CREATED -> CANCELLED`：调用 `PUT /api/orders/{id}/cancel` 取消订单，并回补库存。
- `CREATED -> TIMEOUT`：订单超过 `expireAt` 后由定时任务扫描关闭，并回补库存。
- 支付、取消、超时都使用条件状态更新；重复扫描或并发竞争时不会重复回补库存。

---

## 错误码

| 业务码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误或业务校验失败 |
| 401 | 未登录、令牌无效或令牌过期 |
| 409 | 幂等键冲突或同一订单请求仍在处理中 |
| 404 | 商品、购物车明细或订单不存在 |
| 500 | 服务器内部错误 |

## Postman / ApiFox 覆盖测试清单

| 场景 | 请求 | 预期 |
|------|------|------|
| 商品新增成功 | `POST /api/products/add`，合法 JSON | `code=200`，`message=新增商品成功` |
| 商品新增参数错误 | `POST /api/products/add`，空名称、价格为 0、库存为 -1 | `code=400`，`data` 返回字段错误 |
| 商品删除成功 | `DELETE /api/products/delete/{id}` | `code=200`，`message=删除商品成功` |
| 商品删除不存在 | `DELETE /api/products/delete/99999` | `code=404`，`message=商品不存在` |
| 商品更新成功 | `PUT /api/products/update/{id}`，合法 JSON | `code=200`，返回更新后商品 |
| 商品更新不存在 | `PUT /api/products/update/99999` | `code=404`，`message=商品不存在` |
| 商品分页查询 | `GET /api/products/query?page=1&pageSize=6` | `code=200`，返回 `data.items` 和 `page` |
| 查询参数错误 | `GET /api/products/query?page=0` 或 `GET /api/products/not-number` | `code=400` |
| 业务异常 | `GET /api/products/query?minPrice=100&maxPrice=50` | `code=400`，提示最低价格不能大于最高价格 |
| 接口不存在 | `GET /api/not-exists` | `code=404`，`message=接口不存在` |
| 订单幂等提交成功 | `POST /api/orders`，携带新 `Idempotency-Key` | `code=200`，创建一笔订单 |
| 订单重复提交 | 使用相同 `Idempotency-Key` 和相同请求体重试 `POST /api/orders` | `code=200`，返回同一订单 |
| 订单幂等冲突 | 使用相同 `Idempotency-Key` 和不同请求体重试 `POST /api/orders` | `code=409` |
| 订单支付成功 | `PUT /api/orders/{id}/pay`，订单为 `CREATED` 且未过期 | `code=200`，订单状态变为 `PAID` |
| 订单超时关闭 | 超时定时任务扫描到 `expireAt <= now` 的 `CREATED` 订单 | 订单状态变为 `TIMEOUT`，库存只回补一次 |

---

## 变更记录

| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|----------|--------|
| v1.8 | 2026-06-15 | 新增订单状态机说明、`PUT /api/orders/{id}/pay` 支付接口、`expireAt`/`paidAt` 响应字段和超时规则 | @dev |
| v1.7 | 2026-06-15 | `POST /api/orders` 新增必填 `Idempotency-Key` 请求头和 409 幂等冲突说明 | @dev |
| v1.6 | 2026-06-10 | 明确订单 `createdAt` 的 SQLite 兼容持久化格式和历史 ISO 格式读回兼容策略 | @dev |
| v1.5 | 2026-06-10 | 明确订单 `createdAt` 由后端应用系统时间写入，修复数据库默认时间导致的时区偏差 | @dev |
| v1.4 | 2026-06-10 | 新增商品 add/delete/update/query 接口、统一响应分页元信息和 Postman/ApiFox 覆盖测试清单 | @dev |
| v1.3 | 2026-06-10 | 商品列表接口新增 `page`、`pageSize` 分页参数，并返回 `items`、`total`、`page`、`pageSize`、`totalPages` 分页结构 | @dev |
| v1.2 | 2026-06-09 | 订单接口补齐 CRUD：新增更新订单和删除已取消订单接口；后端升级 Spring Boot 3 + MyBatis Plus | @dev |
| v1.1 | 2026-06-09 | 新增用户登录、Bearer Token 认证和受保护接口说明 | @dev |
| v1.0 | 2026-06-09 | 新增商品、购物车、订单核心接口 | @dev |
