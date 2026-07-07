# 电商购物平台 API 文档

**关联设计文档**：[电商购物平台设计](../02-design-docs/online-shop-platform-design.md)  
**文档版本**：v2.3
**创建时间**：2026-06-09  
**最后更新**：2026-07-07
**负责人**：@dev

---

## 概述

- **基础路径**：`/api`
- **OpenAPI JSON**：`/v3/api-docs`
- **Swagger UI**：`/swagger-ui.html`
- **认证方式**：商品查询接口公开访问；商品新增、更新、删除接口必须携带 ADMIN 角色 `Authorization: Bearer <accessToken>`；购物车、订单、个人中心和地址簿接口必须携带 USER 或 ADMIN 角色 `Authorization: Bearer <accessToken>`
- **内容类型**：`application/json`
- **统一响应**：`{ "code": 200, "message": "success", "data": ..., "page": null }`
- **分页响应**：分页接口会额外返回 `page` 元信息；为兼容既有前端，`data` 中仍保留分页对象。

---

## 商品接口

| 方法 | 路径 | 描述 | 请求参数 |
|------|------|------|----------|
| GET | `/api/products` | 商品分页列表和搜索 | `name`、`category`、`minPrice`、`maxPrice`、`page`、`pageSize` |
| GET | `/api/products/{id}` | 商品详情 | path: `id` |
| GET | `/api/products/cache/metrics` | 商品缓存指标 | — |
| POST | `/api/products/add` | 新增商品 | ADMIN + JSON 请求体 |
| DELETE | `/api/products/delete/{id}` | 删除商品 | ADMIN + path: `id` |
| PUT | `/api/products/update/{id}` | 更新商品 | ADMIN + path: `id` + JSON 请求体 |
| GET | `/api/products/query` | 商品分页查询兼容入口 | `name`、`category`、`minPrice`、`maxPrice`、`page`、`pageSize` |

**商品响应字段**：`id`、`name`、`category`、`price`、`stock`、`imageUrl`、`description`

**商品缓存指标字段**：`detailHits`、`detailMisses`、`detailHitRate`、`listHits`、`listMisses`、`listHitRate`

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

> 商品新增、更新、删除接口仅允许 ADMIN 角色访问；普通 USER 访问返回 `code=403`。

---

## 用户认证接口

| 方法 | 路径 | 描述 | 请求体 |
|------|------|------|--------|
| POST | `/api/auth/register` | 用户注册并签发 access token 与 refresh token | `{ "username": "new_user", "password": "new12345", "nickname": "新用户", "phone": "13700000000" }` |
| POST | `/api/auth/login` | 用户登录并签发 access token 与 refresh token | `{ "username": "demo", "password": "demo123" }` |
| POST | `/api/auth/refresh` | 使用 refresh token 轮换新令牌 | `{ "refreshToken": "..." }` |

**登录响应字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `token` | String | 兼容旧前端字段，值等同于 `accessToken` |
| `accessToken` | String | 短期访问令牌，用于 `Authorization` 请求头 |
| `refreshToken` | String | 刷新令牌；服务端仅保存 SHA-256 摘要，可过期、可撤销 |
| `expiresInSeconds` | Number | access token 有效秒数，默认 7200 |
| `tokenType` | String | 固定为 `Bearer` |
| `role` | String | 用户角色，`USER` 或 `ADMIN` |
| `userId` | Number | 用户 ID |
| `username` | String | 用户名 |
| `nickname` | String | 用户昵称 |

**注册安全规则**：

- `username` 仅允许 4-20 位字母、数字和下划线，服务端使用参数绑定 SQL 查询和插入，防止 SQL 注入。
- `password` 长度为 6-64 位，服务端使用 BCrypt 加盐哈希保存，接口响应不会返回密码字段。
- `phone` 若填写必须符合中国大陆手机号格式。

**调用受保护接口示例**：

```http
Authorization: Bearer eyJ...
```

**测试账号**：

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| `demo` | `demo123` | USER | 用户端购物车、订单链路 |
| `admin` | `admin123` | ADMIN | 管理端商品写接口 |

---

## 购物车接口

| 方法 | 路径 | 描述 | 请求体 |
|------|------|------|--------|
| GET | `/api/cart` | 查询购物车 | — |
| POST | `/api/cart/items` | 加入购物车 | `{ "productId": 1, "quantity": 2 }` |
| PUT | `/api/cart/items/{id}` | 修改购物车数量或选中状态 | `{ "quantity": 3, "selected": true }` |
| DELETE | `/api/cart/items/{id}` | 删除购物车明细 | — |

**购物车响应字段**：`items`、`totalQuantity`、`totalAmount`、`selectedQuantity`、`selectedAmount`

**购物车明细字段**：`id`、`productId`、`productName`、`category`、`price`、`quantity`、`selected`、`stock`、`imageUrl`、`subtotal`

> 购物车按 `created_at DESC, id DESC` 倒序返回。`POST /api/orders` 仅使用 `selected=true` 的购物车明细生成订单，并只删除已选明细，未选商品保留在购物车。

> 购物车接口均需要登录令牌。

---

## 个人中心接口

| 方法 | 路径 | 描述 | 请求体 |
|------|------|------|--------|
| GET | `/api/users/me` | 查询当前用户资料、积分和订单数量 | — |
| PUT | `/api/users/me/password` | 修改当前用户登录密码 | `{ "oldPassword": "demo123", "newPassword": "new12345" }` |

**个人中心响应字段**：`userId`、`username`、`nickname`、`phone`、`role`、`points`、`orderCount`

> 修改密码会校验原密码，使用 BCrypt 保存新密码，并撤销当前用户全部 refresh token。

---

## 收货地址接口

| 方法 | 路径 | 描述 | 请求体 |
|------|------|------|--------|
| GET | `/api/addresses` | 查询当前用户收货地址列表 | — |
| POST | `/api/addresses` | 新增收货地址 | `{ "receiverName": "张三", "receiverPhone": "13800000000", "receiverAddress": "上海市", "defaultAddress": true }` |
| PUT | `/api/addresses/{id}` | 更新收货地址 | 同新增地址请求体 |
| DELETE | `/api/addresses/{id}` | 删除收货地址 | — |

**地址响应字段**：`id`、`receiverName`、`receiverPhone`、`receiverAddress`、`defaultAddress`

> 地址簿数据持久化在 `user_address` 表中，所有查询和变更均按当前登录用户隔离。新增第一个地址时会自动设为默认地址；新增或更新默认地址时，会清除同用户其他默认地址标记。

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
- 订单只会包含当前用户购物车中 `selected=true` 的商品；若未选择任何商品，返回 `code=400`。
- 同一用户、同一 `Idempotency-Key` 且请求体一致时，重复请求返回同一笔订单，不重复扣减库存。
- 同一用户、同一 `Idempotency-Key` 但请求体不一致时，返回 `code=409`。
- 首次请求仍在处理中时，重复请求返回 `code=409`，提示稍后重试。

> 订单接口均需要登录令牌，查询结果只返回当前登录用户的数据。仅 `CREATED` 状态订单允许修改或取消，仅 `CANCELLED` 状态订单允许删除。

**订单状态机规则**：

- `CREATED -> PAID`：调用 `PUT /api/orders/{id}/pay` 模拟支付成功，记录 `paidAt`。
- `CREATED -> CANCELLED`：调用 `PUT /api/orders/{id}/cancel` 取消订单，并回补库存。
- `CREATED -> TIMEOUT`：订单超过 `expireAt` 后由 RabbitMQ TTL/DLX 超时消息关闭，并回补库存。
- 支付、取消、超时都使用条件状态更新；RabbitMQ 重复投递、兜底扫描或并发竞争时不会重复回补库存。

---

## 错误码

| 业务码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误、参数类型错误或业务校验失败 |
| 401 | 未登录、令牌无效或令牌过期 |
| 403 | 普通用户访问 ADMIN 管理接口 |
| 409 | 幂等键冲突或同一订单请求仍在处理中 |
| 404 | 接口不存在，或商品、购物车明细、订单不存在 |
| 500 | 服务器内部错误；响应消息会包含服务端异常摘要 |

## Postman / ApiFox 覆盖测试清单

| 场景 | 请求 | 预期 |
|------|------|------|
| 登录成功 | `POST /api/auth/login`，用户名密码正确 | `code=200`，返回 `accessToken`、`refreshToken`、`role` |
| 注册成功 | `POST /api/auth/register`，用户名未占用且参数合法 | `code=200`，返回登录令牌 |
| 刷新令牌成功 | `POST /api/auth/refresh`，refresh token 有效 | `code=200`，轮换新的 `accessToken` 与 `refreshToken` |
| 修改密码成功 | USER token + `PUT /api/users/me/password`，原密码正确 | `code=200`，refresh token 被撤销 |
| 新增地址成功 | USER token + `POST /api/addresses`，合法地址请求体 | `code=200`，返回地址详情 |
| 购物车取消选中 | USER token + `PUT /api/cart/items/{id}`，`selected=false` | `code=200`，`selectedAmount` 重新计算 |
| 商品新增成功 | ADMIN token + `POST /api/products/add`，合法 JSON | `code=200`，`message=新增商品成功` |
| 商品新增未登录 | `POST /api/products/add`，不带 token | `code=401` |
| 商品新增无权限 | USER token + `POST /api/products/add`，合法 JSON | `code=403` |
| 商品新增参数错误 | ADMIN token + `POST /api/products/add`，空名称、价格为 0、库存为 -1 | `code=400`，`data` 返回字段错误 |
| 商品删除成功 | ADMIN token + `DELETE /api/products/delete/{id}` | `code=200`，`message=删除商品成功` |
| 商品删除不存在 | `DELETE /api/products/delete/99999` | `code=404`，`message=商品不存在` |
| 商品更新成功 | ADMIN token + `PUT /api/products/update/{id}`，合法 JSON | `code=200`，返回更新后商品 |
| 商品更新不存在 | `PUT /api/products/update/99999` | `code=404`，`message=商品不存在` |
| 商品分页查询 | `GET /api/products/query?page=1&pageSize=6` | `code=200`，返回 `data.items` 和 `page` |
| 查询参数错误 | `GET /api/products/query?page=0` 或 `GET /api/products/not-number` | `code=400` |
| 业务异常 | `GET /api/products/query?minPrice=100&maxPrice=50` | `code=400`，提示最低价格不能大于最高价格 |
| 接口不存在 | `GET /api/not-exists` | `code=404`，`message=接口不存在` |
| 订单幂等提交成功 | `POST /api/orders`，携带新 `Idempotency-Key` | `code=200`，创建一笔订单 |
| 订单重复提交 | 使用相同 `Idempotency-Key` 和相同请求体重试 `POST /api/orders` | `code=200`，返回同一订单 |
| 订单幂等冲突 | 使用相同 `Idempotency-Key` 和不同请求体重试 `POST /api/orders` | `code=409` |
| 订单支付成功 | `PUT /api/orders/{id}/pay`，订单为 `CREATED` 且未过期 | `code=200`，订单状态变为 `PAID` |
| 订单超时关闭 | RabbitMQ 超时消息投递到 `expireAt <= now` 的 `CREATED` 订单 | 订单状态变为 `TIMEOUT`，库存只回补一次 |

---

## 变更记录

| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|----------|--------|
| v2.3 | 2026-07-07 | 新增用户注册、个人中心、修改密码、收货地址接口；购物车支持选中状态和按添加时间倒序，下单仅消费已选商品 | @dev |
| v2.2 | 2026-06-15 | 增加 SpringDoc OpenAPI 地址、Swagger UI 地址和部署文档链接，补充错误码说明 | @dev |
| v2.1 | 2026-06-15 | 新增 `/api/products/cache/metrics` 商品缓存指标接口，用于记录缓存命中率验证数据 | @dev |
| v2.0 | 2026-06-15 | 登录响应扩展 access token、refresh token、过期时间和角色字段；新增 `/api/auth/refresh`；商品写接口改为 ADMIN 权限 | @dev |
| v1.9 | 2026-06-15 | 将订单超时关闭说明更新为 RabbitMQ TTL/DLX 超时消息，并明确重复投递幂等 | @dev |
| v1.8 | 2026-06-15 | 新增订单状态机说明、`PUT /api/orders/{id}/pay` 支付接口、`expireAt`/`paidAt` 响应字段和超时规则 | @dev |
| v1.7 | 2026-06-15 | `POST /api/orders` 新增必填 `Idempotency-Key` 请求头和 409 幂等冲突说明 | @dev |
| v1.6 | 2026-06-10 | 明确订单 `createdAt` 的 SQLite 兼容持久化格式和历史 ISO 格式读回兼容策略 | @dev |
| v1.5 | 2026-06-10 | 明确订单 `createdAt` 由后端应用系统时间写入，修复数据库默认时间导致的时区偏差 | @dev |
| v1.4 | 2026-06-10 | 新增商品 add/delete/update/query 接口、统一响应分页元信息和 Postman/ApiFox 覆盖测试清单 | @dev |
| v1.3 | 2026-06-10 | 商品列表接口新增 `page`、`pageSize` 分页参数，并返回 `items`、`total`、`page`、`pageSize`、`totalPages` 分页结构 | @dev |
| v1.2 | 2026-06-09 | 订单接口补齐 CRUD：新增更新订单和删除已取消订单接口；后端升级 Spring Boot 3 + MyBatis Plus | @dev |
| v1.1 | 2026-06-09 | 新增用户登录、Bearer Token 认证和受保护接口说明 | @dev |
| v1.0 | 2026-06-09 | 新增商品、购物车、订单核心接口 | @dev |
