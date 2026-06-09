---
codex-harness: required
scope: workspace
---

# Dev_Lifecycle 项目生命周期规范

本文件是 Codex Harness 的必读 guidance，定义完整的研发生命周期六阶段及每个阶段的准入/准出标准，对工作区内所有 Spring Boot 子项目强制生效。

---

## 概述

Dev_Lifecycle 将 Spring Boot 功能开发划分为六个阶段，每个阶段都有明确的准入条件、准出标准和 Codex 行为规范。Codex 必须在每个阶段严格执行对应的 Lifecycle_Gate 检查，确保研发质量。

```
阶段1：需求分析 → 阶段2：技术设计 → 阶段3：编码实现
                                              ↓
阶段6：CI 发布 ← 阶段5：文档同步 ← 阶段4：测试验证
```

---

## 阶段 1：需求分析

### 准入条件
- 开发者提出新功能需求（无其他前置条件）

### 准出标准
- `harness-collab/01-product-specs/{功能名}.md` 已创建
- 需求文档包含以下内容：
  - 功能背景和目标
  - 用户故事（As a... I want... So that...）
  - 验收标准（Given/When/Then 格式）
  - 非功能性需求（性能、安全、兼容性）
- 开发者已明确确认需求文档内容

### Codex 行为规范

1. **引导填写需求模板**：当开发者提出新功能需求时，Codex 必须引导开发者使用 `harness-collab/01-product-specs/templates/product-spec-template.md` 模板创建需求文档
2. **不得提前生成代码**：在需求文档确认前，Codex 不得生成任何业务代码，包括 Controller、Service、Repository、Domain 类
3. **协助完善需求**：Codex 可以帮助开发者分析需求、提出问题、完善验收标准，但不得替代开发者做需求决策
4. **确认检查点**：需求文档创建后，Codex 必须询问开发者："需求文档已创建，请确认内容是否完整，确认后我们将进入技术设计阶段"

---

## 阶段 2：技术设计

### 准入条件
- 阶段 1 准出标准已满足（需求文档已确认）

### 准出标准
- `harness-collab/02-design-docs/{功能名}.md` 已创建
- 设计文档包含以下内容：
  - 架构图（组件关系、数据流向）
  - 接口定义（API 路径、请求/响应格式）
  - 数据模型（实体类、DTO、数据库表结构）
  - 技术选型说明（框架、库、工具）
- 开发者已明确确认设计文档内容

### Codex 行为规范

1. **基于需求生成设计草稿**：Codex 可以基于已确认的需求文档，生成技术设计文档草稿，供开发者审阅和修改
2. **等待开发者确认**：设计文档草稿生成后，Codex 必须等待开发者确认，不得直接进入编码阶段
3. **不得跳过设计阶段**：即使功能较简单，也必须有对应的设计文档（可以简化，但不可省略）
4. **确认检查点**：设计文档确认后，Codex 必须说明："设计文档已确认，现在可以进入编码实现阶段"

---

## 阶段 3：编码实现

### 准入条件
- 阶段 2 准出标准已满足（设计文档已确认）

### 准出标准
- 所有新增代码符合分层架构约束（controller → service → domain ← repository）
- 所有新增类均有对应的测试类（包路径一致，位于 `src/test/java` 下）
- 代码符合 Java 工程规范（Javadoc、命名规范、构造器注入）
- 每个新建包均有 `package-info.java` 文件

### Codex 行为规范

1. **严格按设计文档生成代码**：Codex 必须严格按照已确认的设计文档生成代码，不得擅自修改架构设计
2. **同步生成测试类**：每生成一个业务类，必须同步生成对应的测试类骨架
3. **分层约束检查**：生成每个类时，检查包路径和依赖关系是否符合分层约束
4. **拒绝违规代码**：若开发者要求生成违反分层约束的代码，Codex 必须拒绝并提供合规替代方案
5. **交付摘要**：完成编码后输出标准格式的交付摘要

---

## 阶段 4：测试验证

### 准入条件
- 阶段 3 准出标准已满足（测试类已创建）

### 准出标准
- 执行 `mvn clean verify -Pharness-new` 通过（无构建失败）
- 行覆盖率 ≥ 80%（JaCoCo 报告验证）
- `harness-collab/03-exec-plans/` 下已记录本次测试执行结果
- 所有测试方法命名符合 `should_[预期行为]_when_[条件]` 格式

### Codex 行为规范

1. **提示运行验证命令**：编码完成后，Codex 必须提示开发者执行验证命令：
   ```
   mvn clean verify -Pharness-new
   ```
2. **协助修复测试失败**：若测试失败，Codex 协助分析失败原因并提供修复方案
3. **覆盖率不足处理**：若覆盖率低于 80%，Codex 协助识别未覆盖的代码路径，并生成补充测试用例
4. **记录测试结果**：测试通过后，提示开发者在 `harness-collab/03-exec-plans/` 下记录测试执行结果

---

## 阶段 5：文档同步

### 准入条件
- 阶段 4 准出标准已满足（测试验证通过）

### 准出标准
- `harness-collab/04-api-docs/` 下对应 API 文档已更新
- `harness-collab/func.md` 功能资产总表已更新（状态更新为"已交付"）
- 所有新增/修改的 API 端点均有 `@Tag` 和 `@Operation` 注解

### Codex 行为规范

1. **协助生成 API 文档草稿**：测试验证通过后，Codex 根据 Controller 代码生成或更新 API 文档草稿（OpenAPI 3.0 格式或 Markdown 格式）
2. **提示开发者确认**：API 文档草稿生成后，提示开发者确认内容是否准确
3. **更新 func.md**：提示开发者更新 `harness-collab/func.md` 中对应功能的状态和文档链接
4. **交付摘要更新**：在交付摘要中标注"文档同步：已同步"

---

## 阶段 6：CI 发布

### 准入条件
- 阶段 5 准出标准已满足（文档同步完成）

### 准出标准
- PR 通过 GitHub Actions 全量质量门禁（`.github/workflows/ci-verify.yml`）
- 矩阵构建通过：JDK 17 × JDK 21 × harness-legacy × harness-new
- 安全扫描无高危漏洞（CVSS < 7）

### Codex 行为规范

1. **提示创建 PR**：文档同步完成后，Codex 提示开发者创建 Pull Request
2. **确认 CI 配置**：提示开发者确认 `.github/workflows/ci-verify.yml` 配置正确
3. **CI 失败协助**：若 CI 构建失败，Codex 协助分析失败原因（Checkstyle 违规、SpotBugs 问题、覆盖率不足等）
4. **发布确认**：CI 通过后，提示开发者合并 PR，并在 `harness-collab/func.md` 中将功能状态更新为"已交付"

---

## 阶段总览

| 阶段 | 准入条件 | 关键产出物 | 验证命令/方式 |
|------|----------|-----------|--------------|
| 1. 需求分析 | 无 | `harness-collab/01-product-specs/{功能名}.md` | 开发者确认 |
| 2. 技术设计 | 需求文档已确认 | `harness-collab/02-design-docs/{功能名}.md` | 开发者确认 |
| 3. 编码实现 | 设计文档已确认 | 业务代码 + 测试类 | 代码审查 |
| 4. 测试验证 | 测试类已创建 | 测试报告 + 覆盖率报告 | `mvn clean verify -Pharness-new` |
| 5. 文档同步 | 测试验证通过 | API 文档 + func.md 更新 | 人工确认 |
| 6. CI 发布 | 文档同步完成 | PR 合并 | GitHub Actions |

---

## Lifecycle_Gate 违规处理

当 Codex 检测到当前阶段产出物不满足准出标准时：

1. **输出缺失项清单**：明确列出哪些文件未创建、哪些内容未更新
2. **提供操作指引**：给出具体的修复步骤和参考文档链接
3. **阻止进入下一阶段**：在问题解决前，不得进入下一阶段的工作
4. **持续提醒**：在每次后续对话中重复提示，直到问题解决

示例提示：
```
⚠️ Lifecycle_Gate 检查未通过（阶段 3 → 阶段 4）

缺失项：
- src/test/java/com/example/myapp/service/UserServiceTest.java 未创建

操作指引：
1. 请为 UserServiceImpl 创建对应的测试类
2. 测试类路径：src/test/java/com/example/myapp/service/UserServiceTest.java
3. 参考规范：.codex/guidance/testing-quality-standards.md

完成后请告知，我将继续协助进行测试验证阶段。
```
