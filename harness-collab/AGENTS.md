# AI 协作协议（AGENTS.md）

本文档定义 Codex 与开发者在 Spring Boot Harness 工程管理模板下的完整协作协议。所有使用本模板的项目，Codex 必须严格遵守本协议中的行为规范和交付标准。

---

## 协议概述

**协议版本**：1.0.0  
**适用范围**：所有在 Harness 工作区下进行的 Spring Boot 项目开发  
**核心原则**：规范即代码、门禁即流程、文档先于代码

Codex 在本工作区内的所有行为受以下约束层级管控（优先级从高到低）：

1. **Lifecycle_Gate 门禁**：每个研发阶段的准入/准出检查点，不可跳过
2. **分层架构约束**：四层架构规则，代码生成时强制执行
3. **文档同步要求**：代码变更后必须同步更新对应文档
4. **编码标准**：Checkstyle 规则和 Javadoc 要求

---

## 研发流程（Dev_Lifecycle 六阶段）

### 阶段概览

```
需求分析 → 技术设计 → 编码实现 → 测试验证 → 文档同步 → CI 发布
   ↓           ↓           ↓           ↓           ↓          ↓
 PRD 文档    设计文档    代码+测试    执行记录    API 文档    CI 通过
```

### 阶段 1：需求分析

**目标**：明确功能边界和验收标准，形成可追溯的需求文档。

**Codex 职责**：
- 引导开发者描述功能背景、用户故事和验收标准
- 在 `harness-collab/01-product-specs/` 下创建需求文档（使用 `product-spec-template.md` 模板）
- 确认需求文档包含：功能背景、目标、用户故事（含验收标准）、非功能性需求、排除范围

**准出标准**：需求文档已创建，开发者明确确认内容无误。

### 阶段 2：技术设计

**目标**：确定技术方案，形成可指导编码的设计文档。

**Codex 职责**：
- 基于需求文档提出技术方案
- 在 `harness-collab/02-design-docs/` 下创建设计文档（使用 `design-doc-template.md` 模板）
- 确认设计文档包含：架构图、接口定义、数据模型、技术选型、风险说明

**准出标准**：设计文档已创建，开发者明确确认方案可行。

### 阶段 3：编码实现

**目标**：按照设计文档生成符合分层架构约束的代码。

**Codex 职责**：
- 严格遵循四层架构约束（controller → service → domain ← repository）
- 每个业务类必须放置在正确的包层级
- 同步生成对应的测试类（与被测类相同包路径）
- 所有公共方法必须包含 Javadoc 注释

**准出标准**：代码符合分层约束，测试类已同步创建。

### 阶段 4：测试验证

**目标**：确保代码质量满足覆盖率要求，记录测试执行结果。

**Codex 职责**：
- 确认单元测试覆盖率 ≥ 80%（执行 `mvn clean verify -Pharness-new` 验证）
- 在 `harness-collab/03-exec-plans/` 下记录测试执行结果（使用 `exec-plan-template.md` 模板）
- 记录发现的问题及解决方案

**准出标准**：测试全部通过，覆盖率 ≥ 80%，执行记录已创建。

### 阶段 5：文档同步

**目标**：确保 API 文档和功能资产总表与代码保持同步。

**Codex 职责**：
- 更新 `harness-collab/04-api-docs/` 中对应的 API 文档
- 更新 `harness-collab/func.md` 中的功能状态和关联文档链接
- 确认所有新增/修改的公共 API 端点均已文档化

**准出标准**：API 文档已更新，`func.md` 已更新。

### 阶段 6：CI 发布

**目标**：通过全量质量门禁，确保代码可安全合并到主干。

**Codex 职责**：
- 提示开发者创建 PR 并确认 CI 通过
- 确认 `mvn clean verify -Pharness-new` 全量通过
- 输出最终交付摘要

**准出标准**：CI 全量通过，PR 已合并。

---

## Codex 行为规范

### 代码生成前置检查

在生成任何业务代码之前，Codex **必须**完成以下检查：

```
✅ 检查 1：harness-collab/01-product-specs/ 下是否存在对应需求文档
✅ 检查 2：harness-collab/02-design-docs/ 下是否存在对应设计文档
✅ 检查 3：设计文档中是否包含接口定义和数据模型
✅ 检查 4：开发者是否已明确确认需求和设计文档
```

如果任何检查未通过，Codex **必须**停止代码生成，并提示开发者先完成对应文档。

### 分层约束执行

Codex 在生成 Java 代码时，**必须**遵守以下分层规则：

| 层级 | 包路径 | 允许依赖 | 禁止依赖 |
|------|--------|----------|----------|
| controller | `{base}.controller` | service | domain（直接）、repository |
| service | `{base}.service` | domain、repository | controller |
| domain | `{base}.domain` | 无（纯 POJO） | controller、service、repository |
| repository | `{base}.repository` | domain | controller、service |
| config | `{base}.config` | 所有层 | — |
| common | `{base}.common` | 无 | controller、service、domain、repository |
| exception | `{base}.exception` | 无 | controller、service、domain、repository |

**禁止行为**：
- ❌ controller 直接注入 repository
- ❌ domain 对象包含 Spring 注解（`@Service`、`@Repository` 等）
- ❌ repository 返回 HTTP 相关对象（`ResponseEntity` 等）
- ❌ service 直接返回 HTTP 状态码

### 测试同步要求

每次生成业务代码时，Codex **必须**同步生成对应测试类：

| 被测类层级 | 测试框架 | 测试类命名 |
|-----------|----------|-----------|
| service | `@ExtendWith(MockitoExtension.class)` | `{ClassName}Test.java` |
| controller | `@WebMvcTest({ClassName}.class)` | `{ClassName}Test.java` |
| repository | `@DataJpaTest` 或 `@MybatisTest` | `{ClassName}Test.java` |

测试方法命名规范：`should_[预期行为]_when_[条件]`

---

## 交付标准

### 每个阶段的准出标准

| 阶段 | 必须满足的准出标准 | 验证方式 |
|------|-------------------|----------|
| 需求分析 | 需求文档已创建并经开发者确认 | 文件存在 + 开发者确认 |
| 技术设计 | 设计文档已创建并经开发者确认 | 文件存在 + 开发者确认 |
| 编码实现 | 代码符合分层约束，测试类已创建 | 代码审查 + 文件存在 |
| 测试验证 | 测试全部通过，覆盖率 ≥ 80% | `mvn clean verify -Pharness-new` |
| 文档同步 | API 文档和 func.md 已更新 | 文件内容审查 |
| CI 发布 | CI 全量通过 | GitHub Actions 状态 |

### 交付摘要格式

每次完成一个阶段或功能交付后，Codex **必须**输出以下格式的交付摘要：

```
## 交付摘要

**功能**：{功能名称}
**阶段**：{当前完成的阶段}
**时间**：{完成时间}

### 本次变更文件
| 文件路径 | 变更类型 | 说明 |
|----------|----------|------|
| {文件路径} | 新增/修改/删除 | {变更说明} |

### 测试状态
- 单元测试：{通过/失败} ({通过数}/{总数})
- 覆盖率：{覆盖率}%（要求 ≥ 80%）

### 文档同步状态
- [ ] harness-collab/04-api-docs/ 已更新
- [ ] harness-collab/func.md 已更新
- [ ] harness-collab/03-exec-plans/ 已记录

### 下一步行动
{描述下一步需要开发者完成的操作}
```

---

## 门禁规则（Lifecycle_Gate 检查点）

### 检查点列表

以下检查点在对应开发事件发生后由 Codex 主动核对，Codex **不得**跳过任何检查点：

| 检查点 ID | 触发时机 | 检查内容 | 阻断条件 |
|-----------|----------|----------|----------|
| GATE-01 | 开始编码前 | 需求文档是否存在 | 文档不存在则阻断 |
| GATE-02 | 开始编码前 | 设计文档是否存在 | 文档不存在则阻断 |
| GATE-03 | 代码生成时 | 包路径是否符合分层约束 | 违规时输出警告 |
| GATE-04 | 代码生成时 | 是否同步生成测试类 | 未生成则提示 |
| GATE-05 | 测试验证后 | 覆盖率是否 ≥ 80% | 低于阈值则阻断 |
| GATE-06 | 文档同步时 | API 文档是否已更新 | 未更新则提示 |
| GATE-07 | 文档同步时 | func.md 是否已更新 | 未更新则提示 |
| GATE-08 | CI 发布前 | `mvn clean verify -Pharness-new` 是否通过 | 失败则阻断 |

### 门禁违规处理

当检查点发现违规时，Codex 的处理策略：

1. **输出违规详情**：明确说明哪个检查点未通过，缺少什么内容
2. **提供修正指引**：给出具体的操作步骤，指向对应的模板或文档
3. **记录违规历史**：在会话中保持对未解决违规的追踪
4. **不强制阻断工作**：Codex 不会强制停止开发者的操作，但会在每次后续对话中重复提示

---

## 文档维护规范

### 文档更新触发条件

| 触发事件 | 需要更新的文档 | 负责人 |
|----------|---------------|--------|
| 新增 API 端点 | `04-api-docs/{模块}-api.md`、`func.md` | 开发者（Codex 提示） |
| 修改 API 端点 | `04-api-docs/{模块}-api.md` | 开发者（Codex 提示） |
| 功能状态变更 | `func.md` | 开发者 |
| 架构规则变更 | `05-methodology/architecture-constraints.md`、`.codex/guidance/java-engineering-standards.md` | 架构师 |
| 流程变更 | `05-methodology/dev-workflow.md`、`.codex/guidance/project-lifecycle.md` | 团队负责人 |

### 文档版本管理

- 所有文档通过 Git 进行版本管理
- 重大变更（架构调整、流程变更）需在文档顶部更新版本号和变更日志
- 模板文件（`templates/` 目录下）的变更需同步通知所有使用该模板的项目

### 文档质量要求

- 所有文档使用中文编写（代码示例除外）
- 文档中的代码示例必须可运行
- 表格和列表优先于长段落
- 每个文档必须包含"最后更新时间"信息
