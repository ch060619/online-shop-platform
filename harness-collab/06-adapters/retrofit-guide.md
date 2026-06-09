# 历史项目接入指南（Retrofit Mode）

本指南适用于将现有 Spring Boot 项目纳入 Harness 工作区监管的场景。历史项目通常存在大量不符合 Harness 规范的代码，因此采用三阶段渐进式迁移策略，避免一次性引入大量阻断性错误，影响正常交付节奏。

---

## 三阶段迁移策略概览

```
observe 阶段          warn 阶段             enforce 阶段
（仅生成报告）    →   （警告不阻断）    →   （违规阻断构建）
harness-legacy       harness-legacy        harness-new
profile              profile               profile

建议时长：1-2周      建议时长：2-4周        长期使用
```

| 阶段 | Maven Profile | Checkstyle | SpotBugs | JaCoCo | 构建行为 |
|------|--------------|-----------|---------|--------|---------|
| observe | harness-legacy | 生成报告，无输出 | 生成报告，无输出 | 生成报告，无阈值 | 始终成功 |
| warn | harness-legacy | 输出警告到控制台 | 输出警告到控制台 | 生成报告，无阈值 | 始终成功 |
| enforce | harness-new | 违规阻断构建 | 违规阻断构建 | 覆盖率 < 80% 阻断 | 违规时失败 |

---

## 阶段一：observe（仅生成报告）

**目标**：了解现有代码的违规情况，制定修复计划，不影响当前交付节奏。

**使用 Profile**：`harness-legacy`

**建议时长**：1-2 周

### 具体操作步骤

#### 1.1 将 Harness 模板部署到工作区根目录

```bash
# 将历史项目移动到工作区根目录的子目录下
# 假设历史项目原来在 /path/to/legacy-project
mkdir my-workspace
cp -r spring-boot-harness-template/. my-workspace/
mv /path/to/legacy-project my-workspace/legacy-service

cd my-workspace
```

#### 1.2 在 Codex 中打开工作区

打开 Codex，选择 `my-workspace/` 作为工作区根目录。

#### 1.3 执行 observe 阶段构建，生成违规报告

```bash
# 使用 harness-legacy profile 执行构建（仅生成报告，不输出警告）
mvn clean verify -Pharness-legacy -pl legacy-service

# 查看 Checkstyle 报告
cat legacy-service/target/checkstyle-result.xml | grep "error" | wc -l
# 输出：Checkstyle 违规总数

# 查看 SpotBugs 报告
cat legacy-service/target/spotbugsXml.xml | grep "BugInstance" | wc -l
# 输出：SpotBugs Bug 总数

# 查看 JaCoCo 覆盖率报告
open legacy-service/target/site/jacoco/index.html
```

#### 1.4 分析违规情况，制定修复计划

根据报告，将违规分类：

| 违规类型 | 数量 | 修复优先级 | 预计工作量 |
|----------|------|-----------|-----------|
| Checkstyle：缺少 Javadoc | — | 中 | — |
| Checkstyle：命名不规范 | — | 低 | — |
| SpotBugs：空指针风险 | — | 高 | — |
| SpotBugs：资源未关闭 | — | 高 | — |
| 覆盖率不足 80% | — | 中 | — |

#### 1.5 在 SpotBugs 排除文件中登记已知误报

对于确认为误报的 SpotBugs 警告，在 `config/spotbugs/exclude.xml` 中添加排除规则：

```xml
<FindBugsFilter>
    <!-- 历史遗留代码：已知误报，待后续重构时修复 -->
    <Match>
        <Class name="com.example.legacy.OldClass"/>
        <Bug pattern="NP_NULL_ON_SOME_PATH"/>
    </Match>
</FindBugsFilter>
```

#### 1.6 在 harness-collab/func.md 中登记历史功能

将历史项目的主要功能登记到 `func.md`，状态设为"已交付"，文档链接暂时留空：

```markdown
| 用户管理（历史） | 已交付 | @legacy-owner | — | — | — | 2024-01-01 |
```

---

## 阶段二：warn（警告不阻断）

**目标**：在日常开发中看到违规警告，逐步修复存量问题，同时不阻断新功能交付。

**使用 Profile**：`harness-legacy`（与 observe 阶段相同，但团队已开始关注警告）

**建议时长**：2-4 周

### 具体操作步骤

#### 2.1 配置 CI 使用 harness-legacy profile

在 `.github/workflows/ci-verify.yml` 中，历史项目的 CI 先只使用 `harness-legacy`：

```yaml
# 历史项目 CI 配置（warn 阶段）
jobs:
  verify:
    strategy:
      matrix:
        java-version: [17, 21]
        profile: [harness-legacy]  # 暂时只用 legacy，不用 new
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK ${{ matrix.java-version }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ matrix.java-version }}
          distribution: temurin
      - name: Run verify with ${{ matrix.profile }}
        run: mvn clean verify -P${{ matrix.profile }} -pl legacy-service -B
```

#### 2.2 建立违规修复工作流

在每次 Sprint 中，分配一定比例的时间修复存量违规：

```
建议分配：每个 Sprint 10-20% 的时间用于修复 Harness 违规
优先级：SpotBugs 高危 Bug > 覆盖率不足 > Checkstyle 违规
```

#### 2.3 新增代码遵循 Harness 规范

从 warn 阶段开始，所有**新增**的代码必须符合 Harness 规范：
- 新增类放置在正确的包层级
- 新增方法包含 Javadoc 注释
- 新增功能同步创建测试类

#### 2.4 逐步补充历史功能文档

利用 Codex 帮助补充历史功能的文档：

```
向 Codex 描述：
"请帮我为现有的 UserController 生成 API 文档，
文件路径：harness-collab/04-api-docs/user-api.md"
```

#### 2.5 跟踪违规数量趋势

每周记录违规数量，确认趋势向好：

```bash
# 每周执行一次，记录违规数量
echo "$(date): Checkstyle violations: $(cat legacy-service/target/checkstyle-result.xml | grep 'error' | wc -l)"
echo "$(date): SpotBugs bugs: $(cat legacy-service/target/spotbugsXml.xml | grep 'BugInstance' | wc -l)"
```

#### 2.6 评估切换到 enforce 阶段的时机

当以下条件满足时，可以考虑切换到 enforce 阶段：
- Checkstyle 违规数量 < 10 个
- SpotBugs 高危 Bug 数量 = 0
- 新增代码的覆盖率 ≥ 80%（存量代码可以暂时豁免）

---

## 阶段三：enforce（违规阻断构建）

**目标**：切换到强制门禁模式，确保所有新增代码符合 Harness 规范，存量违规已全部修复。

**使用 Profile**：`harness-new`

**建议时长**：长期使用

### 具体操作步骤

#### 3.1 切换 CI 到 harness-new profile

更新 `.github/workflows/ci-verify.yml`，加入 `harness-new` 矩阵：

```yaml
# 历史项目 CI 配置（enforce 阶段）
jobs:
  verify:
    strategy:
      matrix:
        java-version: [17, 21]
        profile: [harness-legacy, harness-new]  # 两个 profile 都执行
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK ${{ matrix.java-version }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ matrix.java-version }}
          distribution: temurin
      - name: Run verify with ${{ matrix.profile }}
        run: mvn clean verify -P${{ matrix.profile }} -pl legacy-service -B
      - name: Upload test reports
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports-jdk${{ matrix.java-version }}-${{ matrix.profile }}
          path: |
            legacy-service/target/surefire-reports/
            legacy-service/target/checkstyle-result.xml
            legacy-service/target/site/jacoco/
          retention-days: 7
```

#### 3.2 处理覆盖率豁免（如有必要）

如果存量代码的覆盖率难以在短期内达到 80%，可以在 `pom.xml` 中配置覆盖率豁免：

```xml
<!-- 在 harness-new profile 中，为历史代码配置覆盖率豁免 -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <!-- 豁免历史遗留代码（计划在下一季度重构） -->
            <exclude>com/example/legacy/old/**</exclude>
        </excludes>
        <rules>
            <rule>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

#### 3.3 验证 enforce 阶段生效

```bash
# 验证 harness-new 构建通过
mvn clean verify -Pharness-new -pl legacy-service

# 预期：BUILD SUCCESS
# 如果失败，查看报告并修复剩余违规
```

#### 3.4 更新 harness-collab 文档

切换到 enforce 阶段后，补充完善所有历史功能的文档：
- 为所有主要功能创建需求文档（可以是回溯性文档）
- 为所有 API 创建 API 文档
- 更新 `func.md` 中所有功能的文档链接

---

## 迁移时间建议

| 项目规模 | observe 阶段 | warn 阶段 | enforce 阶段 |
|----------|-------------|----------|-------------|
| 小型项目（< 10 个模块） | 1 周 | 2 周 | 第 4 周起 |
| 中型项目（10-50 个模块） | 1-2 周 | 3-4 周 | 第 6-8 周起 |
| 大型项目（> 50 个模块） | 2 周 | 4-8 周 | 第 10-12 周起 |

---

## 常见问题

### Q1：历史项目有大量 Checkstyle 违规，如何快速修复？

**方案 1：使用 IDE 自动格式化**

大多数 IDE（IntelliJ IDEA、Eclipse）支持导入 Checkstyle 配置并自动格式化代码：
1. 在 IntelliJ IDEA 中安装 CheckStyle-IDEA 插件
2. 导入 `config/checkstyle/checkstyle.xml` 配置
3. 使用"Reformat Code"功能批量修复格式问题

**方案 2：逐模块修复**

按模块优先级逐步修复，从核心业务模块开始：
```bash
# 查看每个包的违规数量
mvn checkstyle:check -Pharness-legacy -pl legacy-service 2>&1 | grep "\.java" | awk -F: '{print $1}' | sort | uniq -c | sort -rn | head -20
```

### Q2：SpotBugs 报告大量误报，如何处理？

1. 仔细分析每个 Bug 报告，区分真实 Bug 和误报
2. 真实 Bug：立即修复，优先处理高危（P1）Bug
3. 误报：在 `config/spotbugs/exclude.xml` 中添加排除规则，并添加注释说明原因

```xml
<Match>
    <!-- 误报：该方法的返回值在调用方已处理，SpotBugs 无法识别 -->
    <Class name="com.example.legacy.SomeClass"/>
    <Method name="someMethod"/>
    <Bug pattern="RV_RETURN_VALUE_IGNORED"/>
</Match>
```

### Q3：历史项目的架构违反了四层约束，如何渐进修复？

不建议一次性重构所有违规代码，建议：

1. **新增代码**：严格遵循四层架构约束
2. **修改现有代码**：在修改时顺带修复架构违规
3. **专项重构**：在 Sprint 中安排专项重构任务，逐模块修复

对于短期内无法修复的架构违规，在 `config/spotbugs/exclude.xml` 中临时豁免，并添加 TODO 注释：

```java
// TODO: [HARNESS-RETROFIT] 此处直接调用 Repository，违反分层约束
// 计划在 2024 Q2 重构时修复，参考 harness-collab/02-design-docs/user-refactor-design.md
@Autowired
private UserRepository userRepository;
```

### Q4：历史项目没有测试，如何快速提升覆盖率？

1. **优先为核心业务逻辑编写测试**：service 层的核心方法
2. **使用 Codex 辅助生成测试**：描述被测方法的功能，让 Codex 生成测试骨架
3. **分阶段设置覆盖率目标**：
   - 第一阶段：达到 50%
   - 第二阶段：达到 65%
   - 第三阶段：达到 80%（enforce 阶段要求）

### Q5：如何在不影响生产发布的情况下进行迁移？

- observe 和 warn 阶段使用 `harness-legacy` profile，构建始终成功，不影响发布
- enforce 阶段切换前，确保所有违规已修复，避免 CI 突然失败
- 建议在非关键发布窗口期切换到 enforce 阶段，预留时间处理意外问题
