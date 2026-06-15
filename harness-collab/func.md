# 功能资产总表

本文档是项目所有功能的资产登记表，记录每个功能的当前状态、负责人和关联文档。每次功能状态发生变更时，必须同步更新本表。

**最后更新**：2026-06-15

---

## 功能列表

| 功能名称 | 状态 | 负责人 | 需求文档 | 设计文档 | API 文档 | 最后更新 |
|----------|------|--------|----------|----------|----------|----------|
| 用户管理 | 已交付 | @dev | [用户管理需求](01-product-specs/user-management-spec.md) | [用户管理设计](02-design-docs/user-management-design.md) | [用户 API](04-api-docs/user-api.md) | 2024-01-01 |
| 电商购物平台 | 🟢 已交付 | @dev | [电商购物平台需求](01-product-specs/online-shop-platform-spec.md) | [电商购物平台设计](02-design-docs/online-shop-platform-design.md) | [电商购物平台 API](04-api-docs/online-shop-api.md) | 2026-06-10 |
| 商品基础 CRUD 与统一响应 | 🟢 已交付 | @dev | [电商购物平台需求](01-product-specs/online-shop-platform-spec.md) | [电商购物平台设计](02-design-docs/online-shop-platform-design.md) | [电商购物平台 API](04-api-docs/online-shop-api.md) | 2026-06-10 |
| 前端购物车状态与路由增强 | 🟢 已交付 | @dev | [电商购物平台需求](01-product-specs/online-shop-platform-spec.md) | [电商购物平台设计](02-design-docs/online-shop-platform-design.md) | 不涉及新增 API | 2026-06-10 |
| 商品详情促销展示 | 🟢 已交付 | @dev | [电商购物平台需求](01-product-specs/online-shop-platform-spec.md) | [电商购物平台设计](02-design-docs/online-shop-platform-design.md) | 不涉及新增 API | 2026-06-10 |
| 用户登录与 Token 认证 | 🟢 已交付 | @dev | [电商购物平台需求](01-product-specs/online-shop-platform-spec.md) | [电商购物平台设计](02-design-docs/online-shop-platform-design.md) | [电商购物平台 API](04-api-docs/online-shop-api.md) | 2026-06-09 |
| 订单 CRUD 管理 | 🟢 已交付 | @dev | [电商购物平台需求](01-product-specs/online-shop-platform-spec.md) | [电商购物平台设计](02-design-docs/online-shop-platform-design.md) | [电商购物平台 API](04-api-docs/online-shop-api.md) | 2026-06-10 |
| 电商购物平台深挖增强 | 🟡 开发中 | @dev | [深挖增强需求](01-product-specs/online-shop-platform-enhancement-spec.md) | [深挖增强设计](02-design-docs/online-shop-platform-enhancement-design.md) | [电商购物平台 API](04-api-docs/online-shop-api.md) | 2026-06-15 |

---

## 状态说明

| 状态 | 含义 | 下一步行动 |
|------|------|-----------|
| 🔵 规划中 | 功能已列入计划，尚未开始开发 | 创建需求文档，进入需求分析阶段 |
| 🟡 开发中 | 功能正在开发，代码尚未完成 | 完成编码实现，进入测试验证阶段 |
| 🟠 测试中 | 代码已完成，正在进行测试验证 | 完成测试，更新执行计划，进入文档同步阶段 |
| 🟢 已交付 | 功能已完成所有阶段，CI 已通过 | 无（持续维护） |
| ⚫ 已废弃 | 功能已下线或不再维护 | 归档相关文档 |

---

## 使用说明

### 新增功能记录

当开始一个新功能时，在表格中新增一行：

```markdown
| {功能名称} | 🔵 规划中 | @{负责人} | — | — | — | {今日日期} |
```

### 更新功能状态

随着功能推进，逐步填充文档链接并更新状态：

1. **需求分析完成**：填写需求文档链接，状态改为 `🟡 开发中`
2. **设计完成**：填写设计文档链接
3. **测试开始**：状态改为 `🟠 测试中`
4. **文档同步完成**：填写 API 文档链接
5. **CI 通过**：状态改为 `🟢 已交付`，更新最后更新日期

### 文档路径格式

文档链接使用相对路径，格式为：

- 需求文档：`01-product-specs/{功能英文名}-spec.md`
- 设计文档：`02-design-docs/{功能英文名}-design.md`
- API 文档：`04-api-docs/{模块英文名}-api.md`

如功能尚无对应文档，填写 `—`（破折号）。
