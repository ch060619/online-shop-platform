# 工程工作流文档（Dev_Lifecycle）

本文档以流程图形式描述 Spring Boot Harness 模板定义的完整研发生命周期（Dev_Lifecycle），供 Codex 和开发者在整个开发过程中随时参考。

---

## Dev_Lifecycle 六阶段流程图

```mermaid
flowchart TD
    Start([🚀 开始新功能开发]) --> Phase1

    subgraph Phase1["阶段 1：需求分析"]
        P1A[开发者描述功能需求] --> P1B[Codex 引导填写需求文档模板]
        P1B --> P1C[创建 01-product-specs/{功能名}-spec.md]
        P1C --> P1D{开发者确认\n需求文档？}
        P1D -->|否，需要修改| P1B
        P1D -->|是| GATE1
    end

    GATE1{{"🔒 GATE-01\n需求文档已确认？"}} -->|通过| Phase2
    GATE1 -->|未通过| P1B

    subgraph Phase2["阶段 2：技术设计"]
        P2A[Codex 基于需求提出技术方案] --> P2B[填写设计文档模板]
        P2B --> P2C[创建 02-design-docs/{功能名}-design.md]
        P2C --> P2D{开发者确认\n设计文档？}
        P2D -->|否，需要调整| P2B
        P2D -->|是| GATE2
    end

    GATE2{{"🔒 GATE-02\n设计文档已确认？"}} -->|通过| Phase3
    GATE2 -->|未通过| P2B

    subgraph Phase3["阶段 3：编码实现"]
        P3A[Codex 按设计文档生成代码] --> P3B{代码符合\n分层架构约束？}
        P3B -->|否| P3C[Checklist 检查警告\n修正包路径]
        P3C --> P3A
        P3B -->|是| P3D[同步生成测试类]
        P3D --> P3E[添加 Javadoc 注释]
        P3E --> GATE3
    end

    GATE3{{"🔒 GATE-03\n代码符合约束？\n测试类已创建？"}} -->|通过| Phase4
    GATE3 -->|未通过| P3A

    subgraph Phase4["阶段 4：测试验证"]
        P4A[执行 mvn clean verify -Pharness-new] --> P4B{测试全部\n通过？}
        P4B -->|否| P4C[修复失败测试]
        P4C --> P4A
        P4B -->|是| P4D{覆盖率\n≥ 80%？}
        P4D -->|否| P4E[补充测试用例]
        P4E --> P4A
        P4D -->|是| P4F[创建 03-exec-plans/{功能名}-exec-plan.md]
        P4F --> GATE5
    end

    GATE5{{"🔒 GATE-05\n覆盖率 ≥ 80%？\n执行记录已创建？"}} -->|通过| Phase5
    GATE5 -->|未通过| P4E

    subgraph Phase5["阶段 5：文档同步"]
        P5A[更新 04-api-docs/{模块}-api.md] --> P5B[更新 func.md 功能状态]
        P5B --> P5C{API 文档\n已更新？}
        P5C -->|否| P5A
        P5C -->|是| GATE6
    end

    GATE6{{"🔒 GATE-06/07\nAPI 文档已更新？\nfunc.md 已更新？"}} -->|通过| Phase6
    GATE6 -->|未通过| P5A

    subgraph Phase6["阶段 6：CI 发布"]
        P6A[创建 Pull Request] --> P6B[GitHub Actions 执行矩阵构建]
        P6B --> P6C{CI 全量\n通过？}
        P6C -->|否| P6D[查看失败报告\n修复问题]
        P6D --> P6B
        P6C -->|是| P6E[Code Review 通过]
        P6E --> P6F[合并到主干]
        P6F --> GATE8
    end

    GATE8{{"🔒 GATE-08\nCI 全量通过？\nPR 已合并？"}} -->|通过| End
    GATE8 -->|未通过| P6D

    End([✅ 功能交付完成\n更新 func.md 状态为"已交付"])

    style Phase1 fill:#E3F2FD,stroke:#1565C0
    style Phase2 fill:#E8F5E9,stroke:#2E7D32
    style Phase3 fill:#FFF3E0,stroke:#E65100
    style Phase4 fill:#F3E5F5,stroke:#6A1B9A
    style Phase5 fill:#E0F2F1,stroke:#00695C
    style Phase6 fill:#FCE4EC,stroke:#880E4F
    style GATE1 fill:#FF8F00,color:#fff
    style GATE2 fill:#FF8F00,color:#fff
    style GATE3 fill:#FF8F00,color:#fff
    style GATE5 fill:#FF8F00,color:#fff
    style GATE6 fill:#FF8F00,color:#fff
    style GATE8 fill:#FF8F00,color:#fff
```

---

## 各阶段详细说明

### 阶段 1：需求分析

**目标**：明确功能边界，形成可追溯的需求文档。

**参与者**：开发者 + Codex

**详细步骤**：

1. 开发者向 Codex 描述新功能的业务背景和需求
2. Codex 引导开发者使用 `01-product-specs/templates/product-spec-template.md` 模板
3. 共同填写：功能背景、目标、用户故事（含 Given-When-Then 验收标准）、非功能性需求、排除范围
4. 开发者审阅并确认需求文档内容无误
5. 在 `func.md` 中新增功能记录，状态设为"规划中"

**产出物**：`harness-collab/01-product-specs/{功能名}-spec.md`

**准出标准（GATE-01）**：需求文档已创建，开发者明确确认内容无误。

**常见问题**：
- Q：需求不清晰怎么办？A：先记录已知部分，在"排除范围"中标注待确认项，后续迭代补充
- Q：需求变更怎么处理？A：更新需求文档，评估对设计文档和代码的影响，同步更新

---

### 阶段 2：技术设计

**目标**：确定技术方案，形成可指导编码的设计文档。

**参与者**：开发者 + Codex

**详细步骤**：

1. Codex 基于需求文档提出技术方案（架构选型、接口设计、数据模型）
2. 使用 `02-design-docs/templates/design-doc-template.md` 模板创建设计文档
3. 填写：架构图（Mermaid）、接口定义（REST API 列表）、数据模型（实体类、DTO、建表 SQL）、技术选型、风险说明
4. 开发者审阅并确认技术方案可行
5. 在 `func.md` 中更新设计文档链接

**产出物**：`harness-collab/02-design-docs/{功能名}-design.md`

**准出标准（GATE-02）**：设计文档已创建，开发者明确确认方案可行。

**常见问题**：
- Q：设计文档需要多详细？A：至少包含接口定义和数据模型，架构图可以简化但不能省略
- Q：设计变更怎么处理？A：更新设计文档，同步更新 API 文档，通知相关开发者

---

### 阶段 3：编码实现

**目标**：按照设计文档生成符合分层架构约束的代码。

**参与者**：Codex（主导）+ 开发者（审查）

**详细步骤**：

1. Codex 按照设计文档中的接口定义和数据模型生成代码
2. 代码必须放置在正确的包层级（controller/service/domain/repository）
3. 每个业务类同步生成对应的测试类（相同包路径）
4. 所有公共方法添加 Javadoc 注释
5. `layer-constraint-check` Checklist 要求 Codex 检查新建文件的包路径合法性

**产出物**：
- `src/main/java/{basePackage}/{layer}/{ClassName}.java`
- `src/test/java/{basePackage}/{layer}/{ClassName}Test.java`

**准出标准（GATE-03）**：代码符合分层约束，测试类已同步创建。

**常见问题**：
- Q：Checklist 报告包路径违规怎么办？A：将文件移动到正确的包路径，更新 import 语句
- Q：测试类应该测什么？A：参考 `testing-quality-standards.md`，service 层用 Mockito，controller 层用 @WebMvcTest

---

### 阶段 4：测试验证

**目标**：确保代码质量满足覆盖率要求，记录测试执行结果。

**参与者**：开发者 + Codex

**详细步骤**：

1. 执行 `mvn clean verify -Pharness-new` 运行全量测试
2. 如果测试失败，分析失败原因并修复
3. 检查覆盖率报告（`target/site/jacoco/index.html`），确认行覆盖率 ≥ 80%
4. 如果覆盖率不足，补充测试用例
5. 创建执行计划文档，记录测试结果和发现的问题
6. 解决所有 P0/P1 问题

**产出物**：`harness-collab/03-exec-plans/{功能名}-exec-plan.md`

**准出标准（GATE-05）**：测试全部通过，覆盖率 ≥ 80%，执行记录已创建。

**常见问题**：
- Q：覆盖率一直达不到 80% 怎么办？A：检查是否有未测试的异常分支，使用 JaCoCo 报告定位未覆盖代码
- Q：Checkstyle 报告大量违规怎么办？A：先用 `harness-legacy` profile 查看警告，逐步修复

---

### 阶段 5：文档同步

**目标**：确保 API 文档和功能资产总表与代码保持同步。

**参与者**：开发者 + Codex

**详细步骤**：

1. 对比设计文档中的接口定义和实际实现，确认是否有变更
2. 更新 `04-api-docs/{模块}-api.md`，确保所有新增/修改的 API 端点已文档化
3. 更新 `func.md`：填写 API 文档链接，更新功能状态为"测试中"
4. `api-doc-sync-check` Checklist 要求 Codex 在 controller 文件修改后提示此步骤

**产出物**：
- `harness-collab/04-api-docs/{模块}-api.md`（更新）
- `harness-collab/func.md`（更新）

**准出标准（GATE-06/07）**：API 文档已更新，`func.md` 已更新。

**常见问题**：
- Q：API 文档需要多详细？A：至少包含请求参数、响应示例和错误码，参考 `api-doc-template.md`
- Q：如果 API 没有变更，还需要更新文档吗？A：不需要，但需要确认文档与代码一致

---

### 阶段 6：CI 发布

**目标**：通过全量质量门禁，确保代码可安全合并到主干。

**参与者**：开发者（主导）+ Codex（辅助）

**详细步骤**：

1. 创建 Pull Request，目标分支为 main/master/develop
2. GitHub Actions 自动触发矩阵构建（JDK 17 × JDK 21 × harness-legacy × harness-new）
3. 等待 CI 全量通过
4. 如果 CI 失败，查看失败报告（Artifacts 中的测试报告和 Checkstyle 报告）
5. 修复问题后推送新提交，CI 自动重新触发
6. CI 通过后，进行 Code Review
7. Code Review 通过后合并 PR
8. 更新 `func.md` 中的功能状态为"已交付"

**准出标准（GATE-08）**：CI 全量通过，PR 已合并。

**常见问题**：
- Q：CI 在 harness-new 失败但 harness-legacy 通过怎么办？A：查看 Checkstyle 或 JaCoCo 报告，修复违规
- Q：安全扫描（security-scan）失败怎么办？A：安全扫描设置了 `continue-on-error: true`，不阻断 PR，但需要评估漏洞风险

---

## 阶段间的流转条件

| 从阶段 | 到阶段 | 流转条件 | 检查点 |
|--------|--------|----------|--------|
| 需求分析 | 技术设计 | 需求文档已创建并经开发者确认 | GATE-01 |
| 技术设计 | 编码实现 | 设计文档已创建并经开发者确认 | GATE-02 |
| 编码实现 | 测试验证 | 代码符合分层约束，测试类已创建 | GATE-03 |
| 测试验证 | 文档同步 | 测试全部通过，覆盖率 ≥ 80%，执行记录已创建 | GATE-05 |
| 文档同步 | CI 发布 | API 文档和 func.md 已更新 | GATE-06/07 |
| CI 发布 | 完成 | CI 全量通过，PR 已合并 | GATE-08 |

---

## 常见问题与解决方案

### Q1：可以跳过某个阶段吗？

不建议跳过任何阶段。每个阶段都有其存在的价值：
- 跳过需求分析：可能导致实现与预期不符，后期返工成本高
- 跳过技术设计：可能导致架构混乱，难以维护
- 跳过测试验证：可能引入质量问题，影响生产稳定性
- 跳过文档同步：导致文档与代码脱节，增加后续维护成本

### Q2：历史项目如何接入 Dev_Lifecycle？

历史项目可以使用 Retrofit_Mode 渐进接入，参考 `harness-collab/06-adapters/retrofit-guide.md`。

### Q3：小型修复（Bug Fix）也需要走完整流程吗？

对于小型 Bug Fix（不涉及新功能、不修改 API），可以简化流程：
- 阶段 1（需求分析）：简单描述 Bug 现象和修复方案即可，无需完整需求文档
- 阶段 2（技术设计）：如果修复方案简单，可以省略设计文档
- 阶段 3-6：必须完整执行

### Q4：如何处理紧急发布？

紧急发布时，可以临时使用 `harness-legacy` profile 降低门禁要求，但必须在发布后补充完善文档和测试。
