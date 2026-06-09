---
codex-harness: required
scope: workspace
---

# Java 工程规范

本文件是 Codex Harness 的必读 guidance。Codex 在阅读根目录 `AGENTS.md` 后，必须将本文件中的工程约束应用到工作区内全部 Spring Boot 子项目。

---

## 一、四层架构强制约束

### 1.1 架构层级定义

合法的依赖方向：
- `controller` → `service`（controller 只能调用 service）
- `service` → `domain`（service 操作领域对象）
- `service` → `repository`（service 通过 repository 访问数据）
- `repository` → `domain`（repository 操作领域对象）

### 1.2 禁止的跨层依赖

**以下依赖关系被严格禁止，Codex 不得生成违反以下规则的代码：**

| 禁止行为 | 说明 |
|----------|------|
| `controller` 直接调用 `repository` | controller 不得注入或调用任何 Repository 接口/类 |
| `controller` 直接调用 `domain` 的持久化方法 | controller 不得直接操作 domain 对象的数据库相关方法 |
| `repository` 调用 `service` | 数据访问层不得反向依赖业务逻辑层 |
| `domain` 调用 `service` 或 `repository` | 领域模型不得依赖上层组件 |

**Codex 行为约束**：当检测到代码违反上述分层约束时，必须主动提示开发者，并拒绝生成违规代码。

---

## 二、包结构约定

### 2.1 标准包结构

所有 Java 类必须放置在符合以下格式的包路径中：`{basePackage}.{layer}`

其中 `{layer}` 必须是以下合法层级之一：

| 层级 | 说明 | 典型类型 |
|------|------|----------|
| `controller` | REST 控制器 | `@RestController` 类 |
| `service` | 业务逻辑 | `@Service` 类及其接口 |
| `domain` | 领域模型 | Entity、DTO、VO、枚举 |
| `repository` | 数据访问 | `@Repository` 接口/类 |
| `config` | Spring 配置 | `@Configuration` 类 |
| `common` | 公共工具 | 工具类、常量、公共枚举 |
| `exception` | 异常处理 | 自定义异常、全局异常处理器 |

### 2.2 package-info.java 要求

**每个包必须包含 `package-info.java` 文件**，说明该包的职责边界和禁止事项。

---

## 三、命名规范

### 3.1 类命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| Controller 类 | `{业务名}Controller` | `UserController` |
| Service 接口 | `{业务名}Service` | `UserService` |
| Service 实现类 | `{业务名}ServiceImpl` | `UserServiceImpl` |
| Repository 接口 | `{业务名}Repository` | `UserRepository` |
| 领域实体 | `{业务名}` | `User` |
| DTO 类 | `{业务名}DTO` 或 `{业务名}Request/Response` | `UserDTO`、`CreateUserRequest` |
| 异常类 | `{描述}Exception` | `UserNotFoundException` |
| 配置类 | `{描述}Config` | `SecurityConfig` |

### 3.2 方法命名规范

- 所有方法名遵循**驼峰命名法（camelCase）**
- 动词开头，清晰表达方法意图
- 示例：`getUserById`、`createOrder`、`deleteExpiredSessions`

### 3.3 常量命名规范

- 所有常量使用 **UPPER_SNAKE_CASE**
- 示例：`MAX_RETRY_COUNT`、`DEFAULT_PAGE_SIZE`、`API_VERSION`

---

## 四、Javadoc 注释要求

**所有公共方法（`public` 修饰）必须包含 Javadoc 注释**，包含以下内容：

- `@param`：每个参数的说明
- `@return`：返回值说明（void 方法除外）
- `@throws`：可能抛出的受检异常说明

示例：

```java
/**
 * 根据用户 ID 查询用户信息。
 *
 * @param userId 用户唯一标识，不得为 null
 * @return 用户信息 DTO，若用户不存在则抛出异常
 * @throws UserNotFoundException 当指定 ID 的用户不存在时
 */
public UserDTO getUserById(Long userId) {
    // ...
}
```

---

## 五、依赖注入规范

**禁止使用 `@Autowired` 字段注入**，必须使用**构造器注入**。

错误示例（禁止）：
```java
@Service
public class OrderService {
    @Autowired
    private UserRepository userRepository; // 禁止！
}
```

正确示例（必须）：
```java
@Service
public class OrderService {
    private final UserRepository userRepository;

    public OrderService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

推荐使用 Lombok `@RequiredArgsConstructor` 简化构造器注入：
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final UserRepository userRepository;
}
```

---

## 六、Codex 执行约束

1. **生成代码前**：确认目标类的包路径符合分层约定
2. **生成代码时**：严格遵循四层架构依赖方向，不得生成跨层调用代码
3. **发现违规时**：主动提示开发者，说明违规原因，并提供符合规范的替代方案
4. **每个新包**：同步生成 `package-info.java` 文件
5. **所有公共方法**：必须包含完整的 Javadoc 注释
