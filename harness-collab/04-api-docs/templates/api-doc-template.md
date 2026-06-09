# [模块名称] API 文档

> **使用说明**：将 `[模块名称]` 替换为实际模块名称（例如：用户管理），按照各章节提示填写内容。
>
> **关联设计文档**：[设计文档链接]  
> **文档版本**：v1.0  
> **创建时间**：YYYY-MM-DD  
> **最后更新**：YYYY-MM-DD  
> **负责人**：@username

---

## 概述

- **基础路径**：`/api/v1/[module]`（将 `[module]` 替换为实际模块路径，例如 `/api/v1/users`）
- **API 版本**：v1
- **认证方式**：Bearer Token（在请求头中携带 `Authorization: Bearer {token}`）
- **内容类型**：`application/json`
- **字符编码**：UTF-8

---

## 接口列表

| 方法 | 路径 | 描述 | 认证 | 引入版本 |
|------|------|------|------|---------|
| GET | `/api/v1/[module]/{id}` | 根据 ID 查询[资源] | 需要 | v1.0 |
| GET | `/api/v1/[module]` | 分页查询[资源]列表 | 需要 | v1.0 |
| POST | `/api/v1/[module]` | 创建[资源] | 需要 | v1.0 |
| PUT | `/api/v1/[module]/{id}` | 更新[资源] | 需要 | v1.0 |
| DELETE | `/api/v1/[module]/{id}` | 删除[资源] | 需要 | v1.0 |

---

## 接口详情

### GET /api/v1/[module]/{id}

**描述**：根据 ID 查询[资源]详情。

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 描述 |
|--------|------|------|------|------|
| id | path | Long | 是 | [资源]唯一标识，必须为正整数 |
| Authorization | header | String | 是 | Bearer Token，格式：`Bearer {token}` |

**请求示例**：

```http
GET /api/v1/[module]/1 HTTP/1.1
Host: api.example.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例（200 OK）**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "示例名称",
    "description": "示例描述",
    "status": "ACTIVE",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | [资源]唯一标识 |
| name | String | 名称 |
| description | String | 描述，可为 null |
| status | String | 状态：ACTIVE（启用）/ INACTIVE（禁用） |
| createdAt | String | 创建时间（ISO 8601 格式） |
| updatedAt | String | 最后更新时间（ISO 8601 格式） |

**错误码**：

| HTTP 状态码 | 业务码 | 说明 |
|------------|--------|------|
| 200 | 200 | 查询成功 |
| 401 | 401 | Token 无效或已过期 |
| 404 | 404 | [资源]不存在 |
| 500 | 500 | 服务器内部错误 |

---

### GET /api/v1/[module]

**描述**：分页查询[资源]列表，支持按名称模糊搜索。

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|------|--------|------|
| page | query | Integer | 否 | 0 | 页码，从 0 开始 |
| size | query | Integer | 否 | 20 | 每页数量，最大 100 |
| sort | query | String | 否 | createdAt,desc | 排序字段和方向，格式：`{field},{direction}` |
| name | query | String | 否 | — | 名称模糊搜索关键词 |
| Authorization | header | String | 是 | — | Bearer Token |

**请求示例**：

```http
GET /api/v1/[module]?page=0&size=10&sort=createdAt,desc&name=示例 HTTP/1.1
Host: api.example.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应示例（200 OK）**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "示例名称",
        "description": "示例描述",
        "status": "ACTIVE",
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

**错误码**：

| HTTP 状态码 | 业务码 | 说明 |
|------------|--------|------|
| 200 | 200 | 查询成功 |
| 400 | 400 | 请求参数错误（如 size 超过最大值） |
| 401 | 401 | Token 无效或已过期 |
| 500 | 500 | 服务器内部错误 |

---

### POST /api/v1/[module]

**描述**：创建新[资源]。

**请求头**：

| 参数名 | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | Bearer Token |
| Content-Type | 是 | `application/json` |

**请求体**：

```json
{
  "name": "新资源名称",
  "description": "资源描述（可选）"
}
```

**请求体字段说明**：

| 字段名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| name | String | 是 | 长度 1-100，不能为空白字符 | [资源]名称 |
| description | String | 否 | 长度 ≤ 500 | [资源]描述 |

**响应示例（201 Created）**：

```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 2,
    "name": "新资源名称",
    "description": "资源描述",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

**错误码**：

| HTTP 状态码 | 业务码 | 说明 |
|------------|--------|------|
| 201 | 201 | 创建成功 |
| 400 | 400 | 请求参数校验失败（响应体中包含具体字段错误） |
| 401 | 401 | Token 无效或已过期 |
| 409 | 409 | [资源]名称已存在 |
| 500 | 500 | 服务器内部错误 |

**参数校验失败响应示例（400 Bad Request）**：

```json
{
  "code": 400,
  "message": "请求参数校验失败",
  "data": {
    "errors": [
      {
        "field": "name",
        "message": "名称不能为空"
      }
    ]
  }
}
```

---

### PUT /api/v1/[module]/{id}

**描述**：更新指定 ID 的[资源]信息。

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 描述 |
|--------|------|------|------|------|
| id | path | Long | 是 | [资源]唯一标识 |
| Authorization | header | String | 是 | Bearer Token |

**请求体**：

```json
{
  "name": "更新后的名称",
  "description": "更新后的描述"
}
```

**响应示例（200 OK）**：

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "name": "更新后的名称",
    "description": "更新后的描述",
    "status": "ACTIVE",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-15T11:00:00Z"
  }
}
```

**错误码**：

| HTTP 状态码 | 业务码 | 说明 |
|------------|--------|------|
| 200 | 200 | 更新成功 |
| 400 | 400 | 请求参数校验失败 |
| 401 | 401 | Token 无效或已过期 |
| 403 | 403 | 无权限修改该资源 |
| 404 | 404 | [资源]不存在 |
| 500 | 500 | 服务器内部错误 |

---

### DELETE /api/v1/[module]/{id}

**描述**：删除指定 ID 的[资源]（逻辑删除）。

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 描述 |
|--------|------|------|------|------|
| id | path | Long | 是 | [资源]唯一标识 |
| Authorization | header | String | 是 | Bearer Token |

**响应示例（200 OK）**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**错误码**：

| HTTP 状态码 | 业务码 | 说明 |
|------------|--------|------|
| 200 | 200 | 删除成功 |
| 401 | 401 | Token 无效或已过期 |
| 403 | 403 | 无权限删除该资源 |
| 404 | 404 | [资源]不存在 |
| 500 | 500 | 服务器内部错误 |

---

## 错误码汇总

### 通用错误码

| HTTP 状态码 | 业务码 | 说明 | 处理建议 |
|------------|--------|------|----------|
| 400 | 400 | 请求参数错误 | 检查请求参数格式和必填项 |
| 401 | 401 | 未认证 | 重新登录获取新 Token |
| 403 | 403 | 无权限 | 联系管理员申请权限 |
| 404 | 404 | 资源不存在 | 确认资源 ID 是否正确 |
| 409 | 409 | 资源冲突 | 检查是否存在重复数据 |
| 500 | 500 | 服务器内部错误 | 联系开发团队排查 |

### 模块特定错误码

| 业务码 | 说明 | 处理建议 |
|--------|------|----------|
| [模块错误码 1] | [说明] | [处理建议] |
| [模块错误码 2] | [说明] | [处理建议] |

---

## 变更记录

| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|----------|--------|
| v1.0 | YYYY-MM-DD | 初始版本，包含基础 CRUD 接口 | @username |
