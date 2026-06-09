---
codex-harness: required
scope: workspace
---

# AI 协作协议

本文件是 Codex Harness 的必读 guidance。Codex 进入本工作区后，必须通过根目录 `AGENTS.md` 引用本文件，并将其中的协作规范应用到所有 Spring Boot 子项目。

---

## 一、代码生成前置检查

### 1.1 业务代码生成前提条件

**Codex 在生成任何业务代码前，必须确认以下条件已满足：**

1. `harness-collab/01-product-specs/` 下存在对应功能的需求文档
2. 需求文档已包含用户故事和验收标准
3. 开发者已明确确认需求文档内容

若上述条件未满足，Codex 必须：
- 拒绝生成业务代码
- 引导开发者先创建需求文档（参考模板：`harness-collab/01-product-specs/templates/product-spec-template.md`）
- 说明原因："根据 AI 协作协议，需求文档确认前不得生成业务代码"

### 1.2 架构相关代码生成前提条件

**Codex 在生成架构相关代码（Controller/Service/Repository 类、数据模型、配置类）前，必须确认：**

1. `harness-collab/02-design-docs/` 下存在对应功能的设计文档
2. 设计文档已包含架构图、接口定义和数据模型
3. 开发者已明确确认设计文档内容

若上述条件未满足，Codex 必须：
- 拒绝生成架构代码
- 引导开发者先创建设计文档（参考模板：`harness-collab/02-design-docs/templates/design-doc-template.md`）

---

## 二、Lifecycle_Gate 检查点

**Codex 不得跳过任何 Lifecycle_Gate 检查点。** 六个阶段的检查点如下：

| 检查点 | 触发时机 | 检查内容 |
|--------|----------|----------|
| Gate-1：需求确认 | 开发者提出新功能需求时 | `harness-collab/01-product-specs/{功能名}.md` 是否存在并已确认 |
| Gate-2：设计确认 | 开发者请求生成代码时 | `harness-collab/02-design-docs/{功能名}.md` 是否存在并已确认 |
| Gate-3：分层合规 | 生成每个 Java 类时 | 类的包路径是否符合分层架构约定 |
| Gate-4：测试同步 | 生成业务类时 | 对应测试类是否已同步生成 |
| Gate-5：文档同步 | 完成代码交付时 | API 文档和 func.md 是否已更新 |
| Gate-6：CI 就绪 | 开发者准备提交 PR 时 | CI 配置是否正确，`mvn clean verify -Pharness-new` 是否通过 |

---

## 三、代码生成同步要求

### 3.1 测试类同步生成

**Codex 在生成业务代码时，必须同步生成对应的测试类。** 规则如下：

- 生成 `UserService` → 同步生成 `UserServiceTest`
- 生成 `UserController` → 同步生成 `UserControllerTest`
- 生成 `UserRepository` → 同步生成 `UserRepositoryTest`
- 测试类包路径与被测类一致（位于 `src/test/java` 下）

### 3.2 分层约束检查

生成每个 Java 类时，Codex 必须：
1. 确认类的包路径包含合法层级关键字
2. 确认类的依赖关系符合四层架构约束
3. 若发现违规，主动提示并拒绝生成违规代码

---

## 四、交付摘要格式

**Codex 每次代码交付后必须输出交付摘要**，格式如下：

```
## 交付摘要
- 修改文件：[文件列表]
- 测试状态：[通过/待补充]
- 文档同步：[已同步/待同步]
- 下一步：[建议操作]
```

示例：

```
## 交付摘要
- 修改文件：
  - src/main/java/com/example/myapp/service/UserServiceImpl.java（新增）
  - src/main/java/com/example/myapp/controller/UserController.java（新增）
  - src/test/java/com/example/myapp/service/UserServiceTest.java（新增）
  - src/test/java/com/example/myapp/controller/UserControllerTest.java（新增）
- 测试状态：待补充（请执行 mvn clean verify -Pharness-new 验证覆盖率）
- 文档同步：待同步（请更新 harness-collab/04-api-docs/user-api.md 和 harness-collab/func.md）
- 下一步：
  1. 执行 mvn clean verify -Pharness-new 确认测试通过且覆盖率 ≥ 80%
  2. 更新 harness-collab/04-api-docs/user-api.md
  3. 更新 harness-collab/func.md 中用户管理功能的状态为"测试中"
```

---

## 五、违规处理规则

### 5.1 分层约束违规

当开发者要求 Codex 生成违反分层约束的代码时（例如：在 Controller 中直接注入 Repository），Codex 必须：

1. **拒绝生成违规代码**
2. 明确说明违规原因，例如："根据分层架构约束，Controller 层不得直接调用 Repository 层，必须通过 Service 层进行业务逻辑处理"
3. 提供符合规范的替代方案

### 5.2 跳过文档检查

当开发者要求 Codex 跳过需求文档或设计文档检查时，Codex 必须：

1. 说明跳过检查的风险
2. 询问开发者是否确认跳过（需明确确认）
3. 若开发者坚持跳过，在交付摘要中明确标注"⚠️ 已跳过 Gate-X 检查，请尽快补充对应文档"

---

## 六、Codex 行为总结

| 场景 | Codex 行为 |
|------|-----------|
| 开发者提出新功能需求 | 引导创建需求文档，不得直接生成代码 |
| 需求文档已确认，请求生成代码 | 引导创建设计文档，不得直接生成代码 |
| 设计文档已确认，请求生成代码 | 按设计文档生成代码，同步生成测试类 |
| 生成代码违反分层约束 | 主动提示，拒绝生成违规代码 |
| 完成代码交付 | 输出标准格式的交付摘要 |
| 开发者准备提交 PR | 提示执行 CI 验证命令，确认门禁通过 |
