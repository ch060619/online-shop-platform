# 新项目接入指南（Bootstrap Mode）

本指南适用于从零开始创建新的 Spring Boot 项目，并将其纳入 Harness 工作区监管的场景。按照以下步骤操作，可在 30 分钟内完成接入并开始第一次规范化开发。

---

## 前置条件

在开始接入之前，请确认以下条件已满足：

| 前置条件 | 验证方式 | 说明 |
|----------|----------|------|
| JDK 17 或 21 已安装 | `java -version` | 推荐使用 JDK 21 LTS |
| Maven 3.8+ 已安装 | `mvn -version` | 或使用项目内置的 Maven Wrapper |
| Codex 已安装 | 打开 Codex | 版本要求：最新稳定版 |
| Git 已安装 | `git --version` | 用于版本管理 |
| 工作区根目录已确定 | — | Harness 模板将部署在此目录 |

---

## 分步接入说明

### 步骤 1：克隆/复制 Harness 模板到工作区根目录

将 Harness 模板复制到你的工作区根目录。Harness 模板本身就是工作区根目录的内容，不是子目录。

**方式 A：从 Git 仓库克隆**

```bash
# 克隆 Harness 模板仓库到工作区目录
git clone https://github.com/your-org/spring-boot-harness-template.git my-workspace
cd my-workspace

# 验证模板文件已就绪
ls -la
# 应该看到：.codex/  harness-collab/  config/  pom.xml  README.md  AGENTS.md
```

**方式 B：手动复制**

```bash
# 将 Harness 模板目录下的所有文件复制到工作区根目录
cp -r spring-boot-harness-template/. my-workspace/
cd my-workspace
```

**验证**：

```bash
# 验证关键目录和文件存在
ls .codex/guidance/    # 应该看到 5 个 .md 文件
ls .codex/checklists/       # 应该看到 4 个 .md 文件
ls harness-collab/    # 应该看到 6 个子目录
cat pom.xml | grep "harness-new"  # 应该找到 harness-new profile 定义
```

---

### 步骤 2：在工作区根目录下创建 Spring Boot 子项目

在工作区根目录下创建新的 Spring Boot 子项目目录。子项目与 Harness 模板并列存在，受 Harness 监管。

**方式 A：使用 Spring Initializr 生成**

```bash
# 访问 https://start.spring.io 生成项目，下载后解压到工作区根目录
# 或使用 curl 命令（示例）
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.2.0 \
  -d baseDir=my-service \
  -d groupId=com.example \
  -d artifactId=my-service \
  -d name=my-service \
  -d packageName=com.example.myservice \
  -d javaVersion=21 \
  -d dependencies=web,data-jpa,validation,lombok \
  -o my-service.zip

unzip my-service.zip
rm my-service.zip
```

**方式 B：手动创建目录结构**

```bash
# 创建标准 Spring Boot 项目目录结构
BASE_PKG="com/example/myservice"
mkdir -p my-service/src/main/java/${BASE_PKG}/{config,common,exception,controller,service,domain,repository}
mkdir -p my-service/src/main/resources
mkdir -p my-service/src/test/java/${BASE_PKG}/support
mkdir -p my-service/src/test/resources
```

**工作区结构应如下所示**：

```
my-workspace/                    ← 工作区根目录（Harness 监管层）
├── .codex/                       ← Harness guidance 和 checklists
├── harness-collab/              ← AI 协作文档体系
├── config/                      ← 静态检查配置
├── pom.xml                      ← Harness Maven 配置
├── README.md
├── AGENTS.md
└── my-service/                  ← 你的 Spring Boot 子项目（受 Harness 监管）
    ├── pom.xml
    └── src/
```

---

### 步骤 3：配置子项目 pom.xml 引用 Harness 配置

子项目的 `pom.xml` 需要引用 Harness 根目录的 Maven Profile 配置，以便使用 Harness 定义的质量门禁。

**在子项目 `pom.xml` 中添加以下配置**：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Spring Boot Parent -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>my-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>my-service</name>

    <properties>
        <java.version>21</java.version>
        <!-- 引用 Harness 根目录的静态检查配置 -->
        <checkstyle.config.location>../config/checkstyle/checkstyle-strict.xml</checkstyle.config.location>
        <spotbugs.excludeFilterFile>../config/spotbugs/exclude.xml</spotbugs.excludeFilterFile>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

    <!-- 引用 Harness 根目录的 Profile 配置 -->
    <!-- 方式：在根目录执行 mvn 命令时指定子模块 -->
    <!-- mvn clean verify -Pharness-new -pl my-service -->

</project>
```

**或者，将子项目配置为 Maven 多模块项目的子模块**（推荐）：

在工作区根目录的 `pom.xml` 中添加子模块：

```xml
<modules>
    <module>my-service</module>
</modules>
```

然后在子项目 `pom.xml` 中设置父 POM：

```xml
<parent>
    <groupId>com.example</groupId>
    <artifactId>harness-parent</artifactId>
    <version>1.0.0</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

---

### 步骤 4：在 Codex 中打开工作区，验证 guidance 文件已加载

1. 打开 Codex
2. 选择"打开文件夹"，选择工作区根目录（`my-workspace/`）
3. 等待 Codex 加载工作区
4. 验证 guidance 文件已加载：

```bash
# 在 Codex 终端中执行
grep -l "codex-harness: required" .codex/guidance/*.md | wc -l
# 预期输出：5
```

5. 向 Codex 发送测试消息，验证约束已生效：

```
请帮我创建一个 UserController 类
```

Codex 应该：
- 询问是否已有需求文档（GATE-01 检查）
- 如果没有，引导创建需求文档
- 生成的代码应放置在 `controller` 包下

---

### 步骤 5：执行 harness-verify.sh 验证模板完整性

```bash
# 在工作区根目录执行
bash harness-verify.sh
```

预期输出（所有项目均为 ✅）：

```
=== Harness 模板完整性验证 ===
✅ .codex/guidance/java-engineering-standards.md
✅ .codex/guidance/testing-quality-standards.md
✅ .codex/guidance/api-doc-sync-protocol.md
✅ .codex/guidance/ai-collaboration-protocol.md
✅ .codex/guidance/project-lifecycle.md
✅ .codex/checklists/api-doc-sync-check.md
✅ .codex/checklists/layer-constraint-check.md
✅ .codex/checklists/test-coverage-reminder.md
✅ .codex/checklists/maven-profile-check.md
...
✅ Harness 模板完整性验证通过
```

---

## 验证命令汇总

完成接入后，执行以下命令验证环境配置正确：

```bash
# 1. 验证模板完整性
bash harness-verify.sh

# 2. 验证 guidance 文件数量
grep -l "codex-harness: required" .codex/guidance/*.md | wc -l
# 预期：5

# 3. 验证子项目可以构建（使用 harness-legacy 宽松模式）
mvn clean verify -Pharness-legacy -pl my-service
# 预期：BUILD SUCCESS

# 4. 验证 harness-new 强制门禁（新项目应该通过）
mvn clean verify -Pharness-new -pl my-service
# 预期：BUILD SUCCESS（如果代码符合规范）
```

---

## 常见问题

### Q1：执行 `mvn clean verify -Pharness-new` 时 Checkstyle 报错

**原因**：生成的代码缺少 Javadoc 注释或不符合命名规范。

**解决方案**：
```bash
# 先用 harness-legacy 查看违规详情（不阻断构建）
mvn checkstyle:check -Pharness-legacy -pl my-service

# 查看详细违规报告
cat my-service/target/checkstyle-result.xml
```

按照报告中的违规位置逐一修复，然后重新执行 `harness-new` 验证。

### Q2：JaCoCo 覆盖率不足 80%

**原因**：测试类未覆盖足够的代码路径。

**解决方案**：
```bash
# 查看覆盖率报告
open my-service/target/site/jacoco/index.html

# 找到覆盖率低的类，补充测试用例
# 重点关注：异常处理分支、边界条件
```

### Q3：Codex 没有按 AGENTS.md 读取 guidance 约束

**原因**：可能是工作区根目录选择不正确，或 guidance 文件缺少 `codex-harness: required` 元数据。

**解决方案**：
```bash
# 确认 guidance 文件包含正确的元数据
head -5 .codex/guidance/java-engineering-standards.md
# 应该看到：
# ---
# codex-harness: required
# ---

# 重新打开工作区（关闭 Codex 后重新打开工作区根目录）
```

### Q4：子项目 pom.xml 找不到 Harness 配置文件

**原因**：相对路径配置不正确。

**解决方案**：确认子项目 `pom.xml` 中的路径配置：
```xml
<!-- 如果子项目在工作区根目录的直接子目录下 -->
<checkstyle.config.location>../config/checkstyle/checkstyle-strict.xml</checkstyle.config.location>

<!-- 如果子项目在更深的目录层级，调整 ../ 的数量 -->
<checkstyle.config.location>../../config/checkstyle/checkstyle-strict.xml</checkstyle.config.location>
```

### Q5：harness-verify.sh 报告缺失文件

**原因**：模板文件未完整复制。

**解决方案**：
```bash
# 查看缺失的文件列表
bash harness-verify.sh 2>&1 | grep "❌"

# 从 Harness 模板仓库重新复制缺失的文件
```
