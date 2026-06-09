# 实施计划：Spring Boot Harness 工程管理模板

## 概述

按组件分组创建所有模板文件，每个任务对应一组相关文件的生成。所有文件创建完成后通过 `harness-verify.sh` 验证模板完整性。

## 任务列表

- [x] 1. 创建 Guidance 文件体系
  - [x] 1.1 创建 `.codex/guidance/java-engineering-standards.md`
    - 包含 `codex-harness: required` 元数据头
    - 定义四层架构强制约束（controller → service → domain ← repository）
    - 禁止跨层直接依赖规则
    - 类命名、方法命名、Javadoc 注释要求
    - 包结构约定：`{basePackage}.{layer}` 格式
    - _需求：1.1, 1.2, 1.3, 1.4, 2.3_

  - [x] 1.2 创建 `.codex/guidance/testing-quality-standards.md`
    - 包含 `codex-harness: required` 元数据头
    - 测试分层策略：service 层 Mockito、controller 层 @WebMvcTest、repository 层 @DataJpaTest/@MybatisTest
    - 测试方法命名规范：`should_[预期行为]_when_[条件]`
    - 测试类与被测类相同包路径要求
    - 解析器/序列化器往返属性测试要求
    - _需求：4.2, 4.3, 4.5, 4.6_

  - [x] 1.3 创建 `.codex/guidance/api-doc-sync-protocol.md`
    - 包含 `codex-harness: required` 元数据头
    - 新增公共 API 必须同步更新 `harness-collab/04-api-docs/` 规则
    - API 变更必须同步更新 `harness-collab/func.md` 规则
    - API 文档格式要求（OpenAPI 3.0 YAML 或标准 Markdown 模板）
    - _需求：1.5, 6.5_

  - [x] 1.4 创建 `.codex/guidance/ai-collaboration-protocol.md`
    - 包含 `codex-harness: required` 元数据头
    - Codex 生成代码前必须确认需求文档和设计文档已存在
    - 不得跳过任何 Lifecycle_Gate 检查点
    - 每次交付后必须输出交付摘要（修改文件列表、测试状态、文档同步状态）
    - _需求：9.3, 9.4, 9.5_

  - [x] 1.5 创建 `.codex/guidance/project-lifecycle.md`
    - 包含 `codex-harness: required` 元数据头
    - 定义 Dev_Lifecycle 六阶段流程及每个阶段的准入/准出标准
    - 六阶段：需求分析 → 技术设计 → 编码实现 → 测试验证 → 文档同步 → CI 发布
    - 每阶段关联文档和验证命令
    - _需求：9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7_

- [x] 2. 创建 Checklists 检查清单体系
  - [x] 2.1 创建 `.codex/checklists/api-doc-sync-check.md`
    - 触发条件：`**/controller/**/*.java` 文件被修改（fileEdited 事件）
    - 提示检查新增/修改的公共 API 端点
    - 提示更新 `harness-collab/04-api-docs/` 和 `harness-collab/func.md`
    - 包含操作指引链接
    - _需求：7.1, 7.2, 7.5_

  - [x] 2.2 创建 `.codex/checklists/layer-constraint-check.md`
    - 触发条件：`**/*.java` 文件被创建（fileCreated 事件）
    - 检查文件路径是否包含合法层级关键字（controller/service/domain/repository/config/common/exception）
    - 违规时输出警告和修正指引
    - _需求：7.1, 7.3, 7.5_

  - [x] 2.3 创建 `.codex/checklists/test-coverage-reminder.md`
    - 触发条件：`**/service/**/*.java,**/domain/**/*.java` 文件被修改（fileEdited 事件）
    - 提示确认对应测试文件是否已同步更新
    - 提示验证覆盖率命令：`mvn clean verify -Pharness-new`
    - _需求：7.1, 7.4, 7.5_

  - [x] 2.4 创建 `.codex/checklists/maven-profile-check.md`
    - 触发条件：`**/pom.xml` 文件被创建（fileCreated 事件）
    - 提示确认是否已继承 Harness Maven Profile 配置
    - 说明 harness-new 和 harness-legacy 两种模式的区别
    - _需求：9.10_

- [x] 3. 创建 Maven 构建配置
  - [x] 3.1 创建根目录 `pom.xml`
    - 配置 `harness-new` profile：Checkstyle 严格规则（failsOnError=true）、SpotBugs（failOnError=true）、JaCoCo 行覆盖率 ≥ 80%
    - 配置 `harness-legacy` profile：Checkstyle 默认规则（仅警告）、SpotBugs（仅警告）、JaCoCo 仅生成报告
    - 配置 `security-scan` profile：OWASP Dependency-Check（CVSS ≥ 7 失败）
    - 所有插件绑定到 `verify` 生命周期
    - _需求：3.3, 3.4, 3.5, 3.6, 3.7, 5.6_

  - [x] 3.2 创建静态检查配置文件
    - 创建 `config/checkstyle/checkstyle.xml`（默认规则，适用于 harness-legacy）
    - 创建 `config/checkstyle/checkstyle-strict.xml`（严格规则，适用于 harness-new，包含 Javadoc 检查）
    - 创建 `config/spotbugs/exclude.xml`（历史项目误报排除清单模板）
    - _需求：3.1, 3.2_

- [x] 4. 创建 CI/CD 配置
  - [x] 4.1 创建 `.github/workflows/ci-verify.yml`
    - 触发条件：PR 提交到 main/master/develop 分支
    - 矩阵构建：JDK 17 × JDK 21 × harness-legacy × harness-new
    - 构建失败时上传测试报告和 Checkstyle 报告（保留 7 天）
    - 包含可选安全扫描 job（continue-on-error: true，使用 -Psecurity-scan）
    - _需求：5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 5. 创建 harness-collab 文档体系
  - [x] 5.1 创建 harness-collab 根目录文件
    - 创建 `harness-collab/README.md`：说明各子目录用途、文档优先级和 guidance 文件同步方式
    - 创建 `harness-collab/AGENTS.md`：完整 AI 协作协议，包含研发流程、交付标准和门禁规则
    - 创建 `harness-collab/func.md`：功能资产总表，包含表头和示例行
    - _需求：6.3, 6.4, 8.4_

  - [x] 5.2 创建需求文档目录及模板
    - 创建 `harness-collab/01-product-specs/README.md`：说明需求文档的编写规范和使用场景
    - 创建 `harness-collab/01-product-specs/templates/product-spec-template.md`：需求文档模板（含用户故事、验收标准格式）
    - _需求：6.1, 6.2_

  - [x] 5.3 创建技术设计文档目录及模板
    - 创建 `harness-collab/02-design-docs/README.md`：说明设计文档的编写规范
    - 创建 `harness-collab/02-design-docs/templates/design-doc-template.md`：技术设计文档模板（含架构图、接口定义、数据模型章节）
    - _需求：6.1, 6.2_

  - [x] 5.4 创建执行计划目录及模板
    - 创建 `harness-collab/03-exec-plans/README.md`：说明执行计划的记录规范
    - 创建 `harness-collab/03-exec-plans/templates/exec-plan-template.md`：执行计划模板（含测试执行结果记录格式）
    - _需求：6.1, 6.2_

  - [x] 5.5 创建 API 文档目录及模板
    - 创建 `harness-collab/04-api-docs/README.md`：说明 API 文档的维护规范
    - 创建 `harness-collab/04-api-docs/templates/api-doc-template.md`：API 文档模板（OpenAPI 3.0 格式示例）
    - _需求：6.1, 6.2_

  - [x] 5.6 创建方法论文档
    - 创建 `harness-collab/05-methodology/architecture-constraints.md`：架构约束文档（四层架构规则、禁止事项）
    - 创建 `harness-collab/05-methodology/dev-workflow.md`：工程工作流文档（含 Dev_Lifecycle 六阶段流程图）
    - 创建 `harness-collab/05-methodology/ai-delivery-playbook.md`：AI 交付手册（Codex 操作规范、交付摘要格式）
    - _需求：6.6, 9.9_

  - [x] 5.7 创建接入指南
    - 创建 `harness-collab/06-adapters/bootstrap-guide.md`：新项目接入指南（Bootstrap_Mode 分步说明）
    - 创建 `harness-collab/06-adapters/retrofit-guide.md`：历史项目接入指南（Retrofit_Mode 三阶段迁移策略：observe → warn → enforce）
    - _需求：1.6, 8.2, 8.6_

- [x] 6. 创建根目录文档
  - [x] 6.1 创建根目录 `README.md`
    - 模板概述和核心定位说明
    - 完整目录结构说明
    - Bootstrap_Mode 和 Retrofit_Mode 两种接入路径的分步说明
    - 常用命令参考（mvn clean verify -Pharness-new/legacy、bash harness-verify.sh）
    - _需求：8.1, 8.2_

  - [x] 6.2 创建根目录 `AGENTS.md`
    - 作为 AI 协作协议入口
    - 链接到 `harness-collab/AGENTS.md`
    - 简要说明 Harness 监管机制
    - _需求：8.3_

- [x] 7. 创建验证脚本
  - [x] 7.1 创建 `harness-verify.sh`
    - 实现 `check_file` 和 `check_dir` 函数
    - 检查所有 guidance 文件（5 个）
    - 检查所有 checklists 文件（4 个）
    - 检查 harness-collab 目录结构和关键文件
    - 检查静态检查配置文件（3 个）
    - 检查 CI/CD 配置文件
    - 检查根目录文档（README.md、AGENTS.md、pom.xml）
    - 输出通过/失败摘要，失败时退出码非 0
    - _需求：设计文档测试策略_

- [x] 8. 检查点 - 运行验证脚本确认模板完整性
  - 执行 `bash harness-verify.sh`，确认所有文件存在，输出全部 ✅
  - 执行 `grep -l "codex-harness: required" .codex/guidance/*.md | wc -l`，确认输出为 5
  - 如有缺失文件，补充创建后重新验证

## 备注

- 标注 `*` 的子任务为可选任务，可在 MVP 阶段跳过
- 每个任务均引用了具体的需求条款，确保可追溯性
- 所有 guidance 文件必须包含 `codex-harness: required` 元数据，确保 Codex 按 AGENTS.md 读取
- 所有 Checklist 文件必须包含正确的 frontmatter（eventType、filePatterns、checklistAction）
- 最终产出应通过 `harness-verify.sh` 全量验证，确保开箱即用
