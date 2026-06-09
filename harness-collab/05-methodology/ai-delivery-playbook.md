# AI 交付手册（ai-delivery-playbook）

本手册定义 Codex 在 Spring Boot Harness 工作区中的操作规范，包括每个研发阶段的具体行为、交付摘要格式和常用命令速查表。

---

## Codex 操作规范

### 阶段 1：需求分析阶段

**Codex 应该做什么**：

1. **接收需求描述**：当开发者描述新功能时，Codex 首先确认是否已有对应的需求文档
   ```
   检查：harness-collab/01-product-specs/ 下是否存在 {功能名}-spec.md
   如果不存在：引导开发者创建需求文档
   如果存在：确认文档是否需要更新
   ```

2. **引导文档创建**：使用标准模板引导开发者填写需求文档
   - 提示开发者描述功能背景（为什么需要这个功能）
   - 引导编写用户故事（As a... I want... So that...）
   - 帮助细化验收标准（Given-When-Then 格式）
   - 确认非功能性需求（性能、安全、兼容性）
   - 明确排除范围

3. **确认文档完整性**：在进入下一阶段前，检查需求文档是否包含所有必填章节

**Codex 不应该做什么**：
- ❌ 在需求文档确认前开始生成代码
- ❌ 跳过用户故事或验收标准的编写
- ❌ 假设需求已经明确，直接进入设计阶段

---

### 阶段 2：技术设计阶段

**Codex 应该做什么**：

1. **分析需求文档**：仔细阅读需求文档，理解功能边界和验收标准

2. **提出技术方案**：基于四层架构约束提出技术方案
   - 确定各层的类名和职责
   - 设计 REST API 端点（路径、方法、请求/响应格式）
   - 设计数据模型（实体类、DTO、数据库表结构）
   - 选择合适的技术栈（JPA/MyBatis、缓存策略等）

3. **创建设计文档**：使用 `design-doc-template.md` 模板创建设计文档
   - 绘制 Mermaid 架构图（必须体现四层架构）
   - 完整定义所有 API 端点
   - 提供建表 SQL

4. **风险评估**：主动识别并记录技术风险和注意事项

**Codex 不应该做什么**：
- ❌ 在设计文档确认前开始生成代码
- ❌ 设计违反四层架构约束的方案
- ❌ 省略接口定义或数据模型章节

---

### 阶段 3：编码实现阶段

**Codex 应该做什么**：

1. **严格遵循设计文档**：按照设计文档中的类名、方法名、包路径生成代码

2. **执行分层约束检查**：每次生成代码前，确认：
   - 类放置在正确的包层级
   - 依赖关系符合分层规则（controller → service → domain ← repository）
   - 没有跨层直接依赖

3. **同步生成测试类**：每个业务类必须同步生成对应的测试类
   - service 层：使用 `@ExtendWith(MockitoExtension.class)`
   - controller 层：使用 `@WebMvcTest`
   - repository 层：使用 `@DataJpaTest` 或 `@MybatisTest`

4. **添加 Javadoc 注释**：所有公共方法必须包含 Javadoc 注释

5. **代码生成顺序**（推荐）：
   ```
   domain（实体类、DTO）
       ↓
   repository（数据访问接口）
       ↓
   service（业务逻辑）
       ↓
   controller（REST 控制器）
       ↓
   exception（异常类、全局处理器）
   ```

**Codex 不应该做什么**：
- ❌ 生成违反分层约束的代码
- ❌ 省略测试类的生成
- ❌ 省略 Javadoc 注释

---

### 阶段 4：测试验证阶段

**Codex 应该做什么**：

1. **引导执行测试**：提示开发者执行测试命令
   ```bash
   mvn clean verify -Pharness-new
   ```

2. **分析测试结果**：帮助开发者分析测试失败原因
   - 查看 `target/surefire-reports/` 中的失败报告
   - 定位失败的测试方法和失败原因
   - 提出修复建议

3. **覆盖率分析**：如果覆盖率不足 80%，帮助识别未覆盖的代码路径
   - 查看 `target/site/jacoco/index.html` 报告
   - 识别未覆盖的方法和分支
   - 建议补充的测试用例

4. **创建执行计划**：引导开发者填写执行计划文档

**Codex 不应该做什么**：
- ❌ 通过降低测试质量来提高覆盖率（如空测试方法）
- ❌ 在覆盖率不足时直接进入下一阶段
- ❌ 忽略 Checkstyle 或 SpotBugs 的违规报告

---

### 阶段 5：文档同步阶段

**Codex 应该做什么**：

1. **对比代码与设计文档**：检查实际实现是否与设计文档一致，记录差异

2. **更新 API 文档**：
   - 确认所有新增/修改的 API 端点已在 `04-api-docs/` 中文档化
   - 更新请求/响应示例（确保与实际代码一致）
   - 更新错误码列表

3. **更新 func.md**：
   - 填写 API 文档链接
   - 更新功能状态
   - 更新最后更新时间

**Codex 不应该做什么**：
- ❌ 跳过 API 文档更新直接进入 CI 发布
- ❌ 在 func.md 中留下空白的文档链接

---

### 阶段 6：CI 发布阶段

**Codex 应该做什么**：

1. **发布前检查**：确认发布检查清单中的所有必填项已完成

2. **引导创建 PR**：提示开发者创建 Pull Request，说明 PR 描述的最佳实践

3. **CI 失败分析**：如果 CI 失败，帮助分析失败原因
   - 查看 GitHub Actions 日志
   - 下载失败报告 Artifacts
   - 提出修复建议

4. **输出最终交付摘要**：PR 合并后，输出完整的交付摘要

**Codex 不应该做什么**：
- ❌ 在 CI 失败时建议跳过检查直接合并
- ❌ 省略最终交付摘要

---

## 交付摘要格式

每次完成一个功能或阶段后，Codex 必须输出以下格式的交付摘要：

```markdown
## 交付摘要

**功能**：{功能名称}
**阶段**：{当前完成的阶段，例如：阶段 3 - 编码实现}
**完成时间**：{YYYY-MM-DD HH:mm}

---

### 本次变更文件

| 文件路径 | 变更类型 | 说明 |
|----------|----------|------|
| src/main/java/.../UserController.java | 新增 | 用户管理 REST 控制器 |
| src/main/java/.../UserService.java | 新增 | 用户管理业务逻辑 |
| src/main/java/.../domain/User.java | 新增 | 用户实体类 |
| src/test/java/.../UserServiceTest.java | 新增 | UserService 单元测试 |

---

### 测试状态

- 单元测试：✅ 通过（12/12）
- 行覆盖率：85%（要求 ≥ 80%）✅
- Checkstyle：✅ 无违规
- SpotBugs：✅ 无高危 Bug

---

### 文档同步状态

- [x] harness-collab/04-api-docs/user-api.md 已更新
- [x] harness-collab/func.md 已更新（状态：测试中）
- [x] harness-collab/03-exec-plans/user-management-exec-plan.md 已创建

---

### 架构合规性

- [x] 代码符合四层架构约束
- [x] 无跨层直接依赖
- [x] 所有公共方法包含 Javadoc 注释

---

### 下一步行动

1. 开发者执行 `mvn clean verify -Pharness-new` 验证本地构建通过
2. 创建 Pull Request，目标分支：main
3. 等待 CI 矩阵构建通过（JDK 17 × JDK 21 × harness-legacy × harness-new）
4. Code Review 通过后合并 PR
5. 合并后更新 func.md 中的功能状态为"已交付"
```

---

## 常用命令速查表

### Maven 构建命令

| 命令 | 说明 | 使用场景 |
|------|------|----------|
| `mvn clean verify -Pharness-new` | 全量构建（强制门禁） | 新项目、发布前验证 |
| `mvn clean verify -Pharness-legacy` | 全量构建（宽松门禁） | 历史项目、日常开发 |
| `mvn test` | 仅执行单元测试 | 快速验证测试 |
| `mvn checkstyle:check -Pharness-new` | 执行 Checkstyle 检查 | 代码规范检查 |
| `mvn spotbugs:check -Pharness-new` | 执行 SpotBugs 检查 | 静态 Bug 检查 |
| `mvn verify -Psecurity-scan` | 执行安全扫描 | 依赖漏洞检查 |
| `mvn clean install -DskipTests` | 跳过测试构建 | 仅编译验证 |

### 报告查看命令

| 命令 | 说明 |
|------|------|
| `open target/site/jacoco/index.html` | 查看 JaCoCo 覆盖率报告（macOS） |
| `start target/site/jacoco/index.html` | 查看 JaCoCo 覆盖率报告（Windows） |
| `cat target/checkstyle-result.xml` | 查看 Checkstyle 检查结果 |
| `cat target/spotbugsXml.xml` | 查看 SpotBugs 检查结果 |

### 模板文件操作

```bash
# 创建需求文档
cp harness-collab/01-product-specs/templates/product-spec-template.md \
   harness-collab/01-product-specs/{功能名}-spec.md

# 创建设计文档
cp harness-collab/02-design-docs/templates/design-doc-template.md \
   harness-collab/02-design-docs/{功能名}-design.md

# 创建执行计划
cp harness-collab/03-exec-plans/templates/exec-plan-template.md \
   harness-collab/03-exec-plans/{功能名}-exec-plan.md

# 创建 API 文档
cp harness-collab/04-api-docs/templates/api-doc-template.md \
   harness-collab/04-api-docs/{模块名}-api.md
```

### 验证命令

```bash
# 验证 Harness 模板完整性
bash harness-verify.sh

# 验证所有 guidance 文件包含 codex-harness: required
grep -l "codex-harness: required" .codex/guidance/*.md | wc -l
# 预期输出：5

# 验证 harness-collab 目录结构
ls -la harness-collab/
```

---

## 与 Codex 协作的最佳实践

### 1. 明确描述需求背景

与 Codex 协作时，提供充分的上下文信息能显著提升代码质量：

```
✅ 好的描述：
"我需要实现用户管理功能，包括用户的增删改查。
用户有用户名、邮箱、手机号字段，用户名和邮箱不能重复。
需要支持按用户名模糊搜索，分页返回结果。
认证方式使用 JWT Bearer Token。"

❌ 不好的描述：
"帮我写用户管理的代码"
```

### 2. 分阶段确认，避免大量返工

每个阶段完成后，仔细确认产出物再进入下一阶段：
- 需求文档确认后再开始设计，避免设计完成后发现需求理解有误
- 设计文档确认后再开始编码，避免代码完成后发现接口设计不合理

### 3. 利用 Checklist 提示

Codex 的 Checklist 要求 Codex 在关键节点主动核对，不要忽略这些提示：
- controller 文件修改后的 API 文档同步提示
- 新建 Java 文件时的包路径检查提示
- service/domain 文件修改后的测试同步提示

### 4. 保持文档与代码同步

文档与代码脱节是最常见的问题，建议：
- 每次修改 API 后立即更新 API 文档，不要积累到最后
- 使用 `func.md` 作为功能状态的单一来源
- 在 PR 描述中包含文档更新的说明

### 5. 充分利用 harness-legacy 模式

在历史项目接入初期，使用 `harness-legacy` 模式可以：
- 了解当前代码的违规情况（不阻断构建）
- 逐步修复违规，而不是一次性全部修复
- 在团队适应后切换到 `harness-new` 模式

### 6. 定期更新 func.md

`func.md` 是项目功能状态的全局视图，建议：
- 每次功能状态变更时立即更新
- 定期（每周）检查 `func.md` 中是否有状态过期的记录
- 废弃的功能及时标记为"已废弃"
