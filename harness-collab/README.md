# harness-collab 文档体系

`harness-collab/` 是 Spring Boot Harness 模板的 AI 协作文档中心，存放整个研发生命周期（Dev_Lifecycle）所需的规范文档、模板和方法论指南。Codex 在每次对话中通过 AGENTS.md 和 guidance 文件感知这些文档的存在，并在适当时机引导开发者创建和维护对应文档。

---

## 目录结构与用途

| 目录 | 用途 | 文档优先级 | 对应 Dev_Lifecycle 阶段 |
|------|------|-----------|------------------------|
| `01-product-specs/` | 存放功能需求文档（PRD），描述用户故事、验收标准和非功能性需求 | 🔴 高（阶段门禁前置条件） | 阶段 1：需求分析 |
| `02-design-docs/` | 存放技术设计文档，包含架构图、接口定义、数据模型和技术选型 | 🔴 高（阶段门禁前置条件） | 阶段 2：技术设计 |
| `03-exec-plans/` | 存放测试执行记录、问题跟踪和发布检查清单 | 🟡 中（测试验证阶段产出物） | 阶段 4：测试验证 |
| `04-api-docs/` | 存放 REST API 接口文档，需与代码变更保持同步 | 🔴 高（文档同步阶段强制要求） | 阶段 5：文档同步 |
| `05-methodology/` | 存放架构约束、工程工作流和 AI 交付手册等方法论文档 | 🟢 低（参考文档，不随功能迭代） | 全阶段参考 |
| `06-adapters/` | 存放新项目接入指南（Bootstrap）和历史项目迁移指南（Retrofit） | 🟢 低（一次性接入参考） | 接入阶段 |

---

## 各子目录详细说明

### `01-product-specs/` — 需求文档

- **何时创建**：开始任何新功能开发前，Codex 会要求先在此目录创建需求文档
- **命名规范**：`{功能英文名}-spec.md`，例如 `user-management-spec.md`
- **模板位置**：`01-product-specs/templates/product-spec-template.md`
- **门禁要求**：需求文档经开发者确认后，方可进入技术设计阶段

### `02-design-docs/` — 技术设计文档

- **何时创建**：需求文档确认后，Codex 会要求在此目录创建技术设计文档
- **命名规范**：`{功能英文名}-design.md`，例如 `user-management-design.md`
- **模板位置**：`02-design-docs/templates/design-doc-template.md`
- **门禁要求**：设计文档经开发者确认后，方可开始生成代码

### `03-exec-plans/` — 执行计划

- **何时创建**：测试验证阶段，记录每次测试执行的结果和问题
- **命名规范**：`{功能英文名}-exec-plan.md`，例如 `user-management-exec-plan.md`
- **模板位置**：`03-exec-plans/templates/exec-plan-template.md`
- **门禁要求**：测试执行记录完成后，方可进入文档同步阶段

### `04-api-docs/` — API 文档

- **何时创建/更新**：每次新增或修改公共 API 端点后，必须同步更新
- **命名规范**：`{模块英文名}-api.md`，例如 `user-api.md`
- **模板位置**：`04-api-docs/templates/api-doc-template.md`
- **门禁要求**：API 文档更新后，同步更新 `func.md` 中的功能状态

### `05-methodology/` — 方法论文档

- **内容**：架构约束规则、Dev_Lifecycle 工作流、AI 交付手册
- **更新频率**：随团队规范演进而更新，不随功能迭代
- **主要文件**：
  - `architecture-constraints.md`：四层架构规则和禁止事项
  - `dev-workflow.md`：Dev_Lifecycle 六阶段流程图和说明
  - `ai-delivery-playbook.md`：Codex 操作规范和交付摘要格式

### `06-adapters/` — 接入指南

- **内容**：新项目接入（Bootstrap）和历史项目迁移（Retrofit）的分步指南
- **更新频率**：模板版本升级时更新
- **主要文件**：
  - `bootstrap-guide.md`：新项目 5 步接入指南
  - `retrofit-guide.md`：历史项目三阶段迁移策略

---

## Guidance 文件同步方式

`harness-collab/` 中的文档与 `.codex/guidance/` 中的 guidance 文件协同工作。当文档内容发生重大变更时，需要同步更新对应的 guidance 文件：

| harness-collab 文档 | 对应 guidance 文件 | 同步触发条件 |
|--------------------|--------------------|-------------|
| `05-methodology/architecture-constraints.md` | `.codex/guidance/java-engineering-standards.md` | 架构规则变更时 |
| `05-methodology/dev-workflow.md` | `.codex/guidance/project-lifecycle.md` | 流程阶段变更时 |
| `AGENTS.md` | `.codex/guidance/ai-collaboration-protocol.md` | 协作协议变更时 |
| `04-api-docs/` 下任意文档 | `.codex/guidance/api-doc-sync-protocol.md` | API 文档规范变更时 |

**同步步骤**：
1. 更新 `harness-collab/` 中的对应文档
2. 检查 `.codex/guidance/` 中对应 guidance 文件的约束规则是否需要同步调整
3. 如需调整，更新 guidance 文件中的相关章节
4. 在 `func.md` 中记录本次文档更新

---

## 文档命名规范

| 文档类型 | 命名格式 | 示例 |
|----------|----------|------|
| 需求文档 | `{功能英文名}-spec.md` | `user-management-spec.md` |
| 设计文档 | `{功能英文名}-design.md` | `user-management-design.md` |
| 执行计划 | `{功能英文名}-exec-plan.md` | `user-management-exec-plan.md` |
| API 文档 | `{模块英文名}-api.md` | `user-api.md` |
| 方法论文档 | `{主题}-{类型}.md` | `architecture-constraints.md` |
| 接入指南 | `{场景}-guide.md` | `bootstrap-guide.md` |

**通用规范**：
- 全部使用小写字母和连字符（kebab-case）
- 不使用空格、下划线或大写字母
- 文件名应简洁且具有描述性
- 模板文件统一放在各目录的 `templates/` 子目录下，实际文档直接放在对应目录根目录
