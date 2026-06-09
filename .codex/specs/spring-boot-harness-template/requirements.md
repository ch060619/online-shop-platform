# 需求文档

## 简介

本功能旨在构建一套面向 Spring Boot 项目的 Codex Harness 工程管理模板。该模板通过 Codex 的原生机制（guidance 文件、spec 规范、checklists 交付核对）将工程规范、编码标准、测试策略和 CI/CD 配置内嵌到 AI 辅助开发流程中，确保 Codex 在每次交付时都能遵循高质量标准，实现"规范即代码、门禁即流程"的研发基线。

**核心定位**：Harness 模板作为工作区根目录的"监管层"，当开发者在同一工作区内创建新的 Spring Boot 子项目时，Harness 的 guidance 文件、checklists 和规范文档通过根目录 AGENTS.md 对该子项目生效，Codex 在整个开发生命周期（需求 → 设计 → 编码 → 测试 → 发布）中均受 Harness 约束，无需额外配置。

模板适用于两类场景：
- **新项目（Bootstrap）**：从零建立规范驱动的 AI 研发流程，Harness 对新建子项目全流程监管
- **历史项目（Retrofit）**：在不破坏存量交付节奏下渐进接入门禁和文档体系

---

## 词汇表

- **Harness_Template**：本模板整体，包含所有 Codex 配置文件、工程规范文档和 CI/CD 配置
- **Codex_Agent**：目标 AI 辅助开发工具，通过 guidance 文件、spec 规范和 checklists 驱动开发流程
- **Guidance_File**：`.codex/guidance/` 目录下的 Markdown 文件，Codex 在每次对话中按 AGENTS.md 读取，用于约束 AI 行为
- **Spec**：`.codex/specs/` 目录下的规范文档集合（requirements、design、tasks），驱动功能开发
- **Checklist**：`.codex/checklists/` 目录下的交付前检查清单，由 Codex 在对应文件变更后主动核对
- **Quality_Gate**：质量门禁，包含静态检查（Checkstyle、SpotBugs）、测试覆盖率（JaCoCo）和文档同步检查
- **Maven_Profile**：Maven 构建配置集，`harness-new` 用于新项目（强制门禁），`harness-legacy` 用于历史项目（宽松门禁）
- **Layered_Architecture**：四层架构约定：controller → service → domain ← repository
- **API_Doc**：接口文档，存放于 `harness-collab/04-api-docs/`，需与代码变更保持同步
- **Func_Registry**：功能资产总表 `harness-collab/func.md`，记录所有已交付功能的状态
- **Bootstrap_Mode**：新项目接入模式，直接使用模板全量配置
- **Retrofit_Mode**：历史项目接入模式，分阶段渐进开启门禁（observe → warn → enforce）
- **Workspace_Root**：Codex 工作区根目录，Harness 模板部署于此，对同工作区内所有子项目生效
- **Sub_Project**：在 Workspace_Root 下新建的 Spring Boot 子项目，受 Harness 全流程监管
- **Dev_Lifecycle**：完整研发生命周期，包含需求分析 → 技术设计 → 编码实现 → 测试验证 → 文档同步 → CI 发布六个阶段
- **Lifecycle_Gate**：每个研发阶段的准入/准出检查点，由 guidance 文件和 checklists 共同执行

---

## 需求列表

### 需求 1：Codex Guidance 文件体系

**用户故事：** 作为使用 Codex 的开发者，我希望模板提供完整的 guidance 文件集，以便 Codex 在每次对话中按 AGENTS.md 和 guidance 遵循 Spring Boot 工程规范，无需手动重复说明约束。

#### 验收标准

1. THE Harness_Template SHALL 在 `.codex/guidance/` 目录下提供以下 guidance 文件：Java 工程规范、测试与质量规范、API 文档同步规范、AI 协作协议。
2. WHEN Codex_Agent 加载项目时，THE Guidance_File SHALL 通过 AGENTS.md 约束 Codex 遵循四层架构（controller → service → domain ← repository）。
3. THE Guidance_File SHALL 明确禁止跨层直接依赖（如 controller 直接调用 repository）。
4. WHEN Codex 生成 Java 代码时，THE Guidance_File SHALL 要求代码符合 Checkstyle 规则，包含必要的 Javadoc 注释。
5. THE Guidance_File SHALL 规定所有新增公共 API 必须同步更新 `harness-collab/04-api-docs/` 和 `harness-collab/func.md`。
6. WHERE 项目为历史项目（Retrofit_Mode），THE Guidance_File SHALL 提供渐进式接入指引，说明 observe → warn → enforce 三阶段策略。

---

### 需求 2：Spring Boot 项目结构规范

**用户故事：** 作为开发者，我希望模板定义清晰的 Spring Boot 项目目录结构和包结构规范，以便 Codex 生成的代码始终符合团队约定的工程布局。

#### 验收标准

1. THE Harness_Template SHALL 提供标准 Maven 项目骨架，包含 `src/main/java/`、`src/main/resources/`、`src/test/java/`、`src/test/resources/` 四个标准目录。
2. THE Harness_Template SHALL 在根包下定义以下子包结构：`config/`、`common/`、`exception/`、`controller/`、`service/`、`domain/`、`repository/`。
3. WHEN Codex 创建新的业务类时，THE Guidance_File SHALL 要求将类放置在与其职责对应的正确包层级中。
4. THE Harness_Template SHALL 提供每个包的 `package-info.java` 文件，说明该包的职责边界和禁止事项。
5. THE Harness_Template SHALL 提供 `application.yml`、`application-dev.yml`、`application-test.yml`、`application-prod.yml` 四个环境配置文件模板。
6. IF Codex 生成的代码违反分层约束，THEN THE Checklist SHALL 触发警告提示，要求开发者确认或修正。

---

### 需求 3：编码标准与静态检查配置

**用户故事：** 作为开发者，我希望模板内置 Checkstyle 和 SpotBugs 配置，以便通过自动化静态检查强制执行编码标准，减少代码审查中的低级问题。

#### 验收标准

1. THE Harness_Template SHALL 在 `config/checkstyle/` 目录下提供 `checkstyle.xml`（默认规则）和 `checkstyle-strict.xml`（严格规则）两套配置文件。
2. THE Harness_Template SHALL 在 `config/spotbugs/` 目录下提供 `exclude.xml`，用于管理历史项目的误报排除清单。
3. THE Harness_Template SHALL 在 `pom.xml` 中配置 `maven-checkstyle-plugin` 和 `spotbugs-maven-plugin`，绑定到 `verify` 生命周期。
4. WHEN 执行 `mvn clean verify -Pharness-new` 时，THE Quality_Gate SHALL 在 Checkstyle 或 SpotBugs 检查失败时阻断构建。
5. WHEN 执行 `mvn clean verify -Pharness-legacy` 时，THE Quality_Gate SHALL 仅输出警告，不阻断构建。
6. THE Harness_Template SHALL 在 `pom.xml` 中配置 JaCoCo，要求 `harness-new` profile 下行覆盖率不低于 80%，低于阈值时阻断构建。
7. IF JaCoCo 覆盖率检查失败，THEN THE Quality_Gate SHALL 输出覆盖率报告路径，提示开发者查看详情。

---

### 需求 4：测试规范与测试骨架

**用户故事：** 作为开发者，我希望模板提供完整的测试规范和测试骨架代码，以便 Codex 生成的测试代码符合分层测试策略，覆盖单元测试、集成测试和 API 测试。

#### 验收标准

1. THE Harness_Template SHALL 提供测试骨架，包含 Spring Boot 上下文加载测试（`@SpringBootTest`）和示例单元测试（`@ExtendWith(MockitoExtension.class)`）。
2. THE Guidance_File SHALL 规定测试分层策略：service 层使用 Mockito 单元测试，controller 层使用 `@WebMvcTest`，repository 层使用 `@DataJpaTest` 或 `@MybatisTest`。
3. WHEN Codex 生成业务代码时，THE Guidance_File SHALL 要求同步生成对应的测试类，测试类与被测类保持相同的包路径。
4. THE Harness_Template SHALL 在 `src/test/java/.../support/` 目录下提供测试辅助类模板，包含测试数据构建器（Builder）和 `@TestConfiguration` 示例。
5. THE Guidance_File SHALL 要求所有测试方法命名遵循 `should_[预期行为]_when_[条件]` 格式。
6. WHERE 项目包含解析器或序列化器，THE Guidance_File SHALL 要求编写往返属性测试（parse → format → parse 结果等价）。

---

### 需求 5：CI/CD 配置

**用户故事：** 作为开发者，我希望模板提供开箱即用的 GitHub Actions CI 配置，以便每次 PR 都能自动执行全量质量门禁检查，确保主干代码质量。

#### 验收标准

1. THE Harness_Template SHALL 在 `.github/workflows/ci-verify.yml` 中提供 GitHub Actions 工作流配置。
2. WHEN PR 提交到主干分支时，THE CI_Workflow SHALL 在 JDK 17 和 JDK 21 两个版本上分别执行 `mvn clean verify`。
3. THE CI_Workflow SHALL 同时执行 `harness-legacy` 和 `harness-new` 两个 Maven Profile 的矩阵构建。
4. THE CI_Workflow SHALL 包含可选的安全扫描 job（`continue-on-error: true`），使用 `-Psecurity-scan` profile。
5. IF CI 构建失败，THEN THE CI_Workflow SHALL 上传测试报告和 Checkstyle 报告作为 Artifacts，保留 7 天。
6. THE Harness_Template SHALL 在 `pom.xml` 中配置 `security-scan` profile，集成 OWASP Dependency-Check 插件。

---

### 需求 6：AI 协作文档体系（harness-collab）

**用户故事：** 作为开发者，我希望模板提供结构化的 AI 协作文档目录，以便团队按照标准化流程完成需求规格、技术设计、执行计划和 API 文档的编写与维护。

#### 验收标准

1. THE Harness_Template SHALL 在 `harness-collab/` 目录下提供以下子目录：`01-product-specs/`、`02-design-docs/`、`03-exec-plans/`、`04-api-docs/`、`05-methodology/`、`06-adapters/`。
2. THE Harness_Template SHALL 为每个子目录提供 `templates/` 子目录，包含对应文档类型的 Markdown 模板文件。
3. THE Harness_Template SHALL 提供 `harness-collab/AGENTS.md`，定义完整的 AI 协作协议，包含研发流程、交付标准和门禁规则。
4. THE Harness_Template SHALL 提供 `harness-collab/func.md`，作为功能资产总表，记录功能名称、状态、负责人和关联文档。
5. WHEN API 相关代码发生变更时，THE Checklist SHALL 提示开发者同步更新 `harness-collab/04-api-docs/` 中的对应文档。
6. THE Harness_Template SHALL 在 `harness-collab/05-methodology/` 下提供架构约束文档（`architecture-constraints.md`）、工程工作流文档（`dev-workflow.md`）和 AI 交付手册（`ai-delivery-playbook.md`）。

---

### 需求 7：Codex Checklists 交付核对

**用户故事：** 作为开发者，我希望模板提供预配置的 Codex Checklists，以便在关键开发节点在交付前主动执行质量检查和文档同步核对，减少人工遗漏。

#### 验收标准

1. THE Harness_Template SHALL 在 `.codex/checklists/` 目录下提供以下 Checklist 配置：API 文档同步检查 Checklist、代码分层约束检查 Checklist、测试覆盖提醒 Checklist。
2. WHEN controller 层文件被修改时，THE Checklist SHALL 要求 Codex 在交付前提示开发者检查并更新对应的 API 文档。
3. WHEN 新的 Java 文件被创建时，THE Checklist SHALL 检查文件所在包是否符合分层架构约定，并在违规时输出警告。
4. WHEN 业务代码文件被修改时，THE Checklist SHALL 提示开发者确认是否需要同步更新对应的测试文件。
5. THE Checklist SHALL 在提示信息中包含具体的操作指引，说明需要更新哪些文件以及如何更新。

---

### 需求 8：模板使用文档与快速入门

**用户故事：** 作为新接入模板的开发者，我希望模板提供清晰的使用文档和快速入门指南，以便在 30 分钟内完成模板接入并开始第一次规范化开发。

#### 验收标准

1. THE Harness_Template SHALL 提供根目录 `README.md`，包含模板概述、目录结构说明、快速入门步骤和常用命令参考。
2. THE Harness_Template SHALL 在 `README.md` 中提供新项目（Bootstrap_Mode）和历史项目（Retrofit_Mode）两种接入路径的分步说明。
3. THE Harness_Template SHALL 提供 `AGENTS.md` 根文件，作为 AI 协作协议入口，链接到 `harness-collab/AGENTS.md`。
4. THE Harness_Template SHALL 在 `harness-collab/README.md` 中说明各子目录的用途、文档优先级和 guidance 文件的同步方式。
5. WHEN 开发者执行 `mvn clean verify` 时，THE Quality_Gate SHALL 在 30 秒内完成基础检查（不含集成测试），输出清晰的通过或失败摘要。
6. THE Harness_Template SHALL 在文档中提供 Retrofit_Mode 的三阶段迁移策略说明：observe（仅报告）→ warn（警告不阻断）→ enforce（阻断构建）。

---

### 需求 9：新项目全流程监管

**用户故事：** 作为开发者，当我在当前工作区创建新的 Spring Boot 子项目时，我希望 Harness 模板能通过 AGENTS.md 对该子项目的整个研发生命周期进行监管，Codex 在每个阶段都能获得明确的规范指引，无需额外配置。

#### 验收标准

1. THE Harness_Template SHALL 在 `.codex/guidance/` 中提供 `project-lifecycle.md` guidance 文件，定义完整的 Dev_Lifecycle 六阶段流程及每个阶段的 Lifecycle_Gate 准入/准出标准。
2. WHEN 开发者在 Workspace_Root 下创建新的 Sub_Project 目录时，THE Guidance_File SHALL 通过根目录 AGENTS.md 对该 Sub_Project 生效，Codex 无需额外加载配置。
3. THE Guidance_File SHALL 规定需求分析阶段：Codex 必须先在 `harness-collab/01-product-specs/` 下创建需求文档，经确认后方可进入编码阶段。
4. THE Guidance_File SHALL 规定技术设计阶段：Codex 必须在 `harness-collab/02-design-docs/` 下创建设计文档，包含架构图、接口定义和数据模型，经确认后方可生成代码。
5. THE Guidance_File SHALL 规定编码实现阶段：Codex 生成的所有代码必须符合分层架构约束，并同步生成对应测试类。
6. THE Guidance_File SHALL 规定测试验证阶段：Codex 必须确保新增代码的单元测试覆盖率不低于 80%，并在 `harness-collab/03-exec-plans/` 下记录测试执行结果。
7. THE Guidance_File SHALL 规定文档同步阶段：Codex 在完成代码交付后必须同步更新 `harness-collab/04-api-docs/` 和 `harness-collab/func.md`。
8. THE Checklist SHALL 在每个 Lifecycle_Gate 节点由 Codex 主动执行检查，若当前阶段产出物不满足准出标准，则阻止进入下一阶段并输出具体的修正指引。
9. THE Harness_Template SHALL 提供 `harness-collab/05-methodology/dev-workflow.md`，以流程图形式描述完整的 Dev_Lifecycle，供 Codex 和开发者随时参考。
10. WHEN Sub_Project 的 `pom.xml` 被创建时，THE Checklist SHALL 提示开发者确认是否已继承 Harness 的 Maven Profile 配置（`harness-new` 或 `harness-legacy`）。
