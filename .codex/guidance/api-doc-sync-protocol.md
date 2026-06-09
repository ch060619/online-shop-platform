---
codex-harness: required
scope: workspace
---

# API 文档同步规范

本文件是 Codex Harness 的必读 guidance，规定 API 文档同步规则。Codex 在新增、修改或删除接口时必须按本文件执行文档同步检查，防止文档与代码脱节。

---

## 一、API 文档同步触发规则

### 1.1 新增 API 端点

**当新增任何公共 REST API 端点时，必须同步执行以下操作：**

1. 在 `harness-collab/04-api-docs/` 下创建或更新对应的 API 文档文件
2. 在 `harness-collab/func.md` 功能资产总表中新增或更新对应功能记录
3. API 文档文件命名规范：`{功能模块名}-api.md`（例如：`user-api.md`、`order-api.md`）

### 1.2 修改 API 端点

**当修改以下任意内容时，必须同步更新 API 文档：**

| 变更类型 | 必须更新的文档 |
|----------|---------------|
| 请求路径变更 | `harness-collab/04-api-docs/{模块}-api.md` + `harness-collab/func.md` |
| 请求参数变更（新增/删除/重命名） | `harness-collab/04-api-docs/{模块}-api.md` |
| 响应格式变更（字段新增/删除/类型变更） | `harness-collab/04-api-docs/{模块}-api.md` |
| HTTP 方法变更 | `harness-collab/04-api-docs/{模块}-api.md` + `harness-collab/func.md` |
| 认证/授权要求变更 | `harness-collab/04-api-docs/{模块}-api.md` |

### 1.3 删除 API 端点

**删除 API 端点必须遵循以下流程：**

1. 先在代码中标记 `@Deprecated` 注解
2. 在 API 文档中注明废弃时间和替代方案
3. 在 `harness-collab/func.md` 中将功能状态更新为"已废弃"
4. 经过至少一个版本周期后，方可实际删除代码和文档

---

## 二、API 文档格式要求

### 2.1 优先使用 OpenAPI 3.0 YAML 格式

新建 API 文档优先使用 OpenAPI 3.0 YAML 格式：

```yaml
openapi: 3.0.3
info:
  title: 用户管理 API
  version: "1.0.0"
paths:
  /api/users/{id}:
    get:
      summary: 根据 ID 查询用户
      operationId: getUserById
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
            format: int64
      responses:
        "200":
          description: 查询成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserDTO'
        "404":
          description: 用户不存在
```

### 2.2 使用标准 Markdown 模板

若不使用 OpenAPI 格式，必须使用 `harness-collab/04-api-docs/templates/api-doc-template.md` 模板，确保文档结构一致。

---

## 三、Controller 类注解要求

### 3.1 @Tag 注解（Swagger/SpringDoc）

**每个 Controller 类上必须有 `@Tag` 注解**，用于 API 文档分组：

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户的增删改查接口")
public class UserController {
    // ...
}
```

### 3.2 @Operation 注解

**每个 `@RequestMapping` 方法（包括 `@GetMapping`、`@PostMapping` 等）必须有 `@Operation` 注解**：

```java
@GetMapping("/{id}")
@Operation(
    summary = "根据 ID 查询用户",
    description = "根据用户唯一标识查询用户详细信息",
    responses = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "用户不存在")
    }
)
public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
    // ...
}
```

### 3.3 废弃接口标记

废弃的接口必须同时标记代码注解和文档：

```java
@GetMapping("/old-endpoint")
@Deprecated
@Operation(
    summary = "【已废弃】旧版查询接口",
    description = "该接口已于 2024-06-01 废弃，请使用 /api/users/{id} 替代",
    deprecated = true
)
public ResponseEntity<UserDTO> oldGetUser(@RequestParam Long userId) {
    // ...
}
```

---

## 四、func.md 功能资产总表维护规则

`harness-collab/func.md` 是功能资产的唯一真实来源，必须与代码保持同步。

### 4.1 记录格式

```markdown
| 功能名称 | 状态 | 负责人 | 需求文档 | 设计文档 | API 文档 | 最后更新 |
|----------|------|--------|----------|----------|----------|----------|
| 用户管理 | 已交付 | @dev | 01-product-specs/user.md | 02-design-docs/user.md | 04-api-docs/user-api.md | 2024-01-15 |
```

### 4.2 状态流转

功能状态必须按以下流程流转：

```
规划中 → 开发中 → 测试中 → 已交付 → 已废弃
```

---

## 五、Codex 执行约束

1. **生成 Controller 代码时**：必须包含 `@Tag` 注解（类级别）和 `@Operation` 注解（方法级别）
2. **新增 API 端点后**：提示开发者同步更新 `harness-collab/04-api-docs/` 和 `harness-collab/func.md`
3. **修改 API 端点后**：在交付摘要中明确列出需要同步更新的文档
4. **删除 API 端点前**：必须先添加 `@Deprecated` 注解，并在文档中注明废弃时间
5. **文档同步检查**：在每次代码交付摘要中，明确标注"文档同步：已同步 / 待同步"
