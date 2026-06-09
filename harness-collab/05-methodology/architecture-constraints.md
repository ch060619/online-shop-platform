# 架构约束文档

本文档定义 Spring Boot Harness 模板强制执行的四层架构规则。所有在 Harness 工作区下开发的 Spring Boot 项目必须遵守这些约束，Codex 在生成代码时会主动检查并执行这些规则。

---

## 四层架构概览

```
┌─────────────────────────────────────────────────────────┐
│                    客户端（HTTP 请求）                     │
└─────────────────────────┬───────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│  controller 层（表现层）                                   │
│  职责：接收 HTTP 请求，参数校验，调用 service，返回响应      │
│  包路径：{basePackage}.controller                         │
└─────────────────────────┬───────────────────────────────┘
                          │ 调用（单向）
                          ▼
┌─────────────────────────────────────────────────────────┐
│  service 层（业务逻辑层）                                  │
│  职责：实现业务逻辑，编排 domain 和 repository 操作         │
│  包路径：{basePackage}.service                            │
└──────────┬──────────────────────────┬───────────────────┘
           │ 使用                      │ 调用（单向）
           ▼                          ▼
┌──────────────────────┐  ┌──────────────────────────────┐
│  domain 层（领域层）  │  │  repository 层（数据访问层）   │
│  职责：领域模型、DTO  │  │  职责：数据库 CRUD 操作        │
│  包路径：.domain     │  │  包路径：.repository           │
│  ← 被 service 使用   │  │  ← 被 service 调用            │
└──────────────────────┘  └──────────────────────────────┘
                                       │
                                       ▼
                          ┌────────────────────────┐
                          │  数据库（MySQL/PostgreSQL）│
                          └────────────────────────┘
```

**合法依赖方向**：
- `controller` → `service`（controller 调用 service）
- `service` → `domain`（service 使用 domain 对象）
- `service` → `repository`（service 调用 repository）
- `repository` → `domain`（repository 操作 domain 实体）

---

## 各层详细说明

### controller 层（表现层）

**职责**：
- 接收并解析 HTTP 请求（路径参数、查询参数、请求体）
- 执行请求参数校验（使用 `@Valid` 注解）
- 调用 service 层执行业务逻辑
- 将 service 返回结果包装为统一响应格式
- 处理 HTTP 状态码映射

**允许的注解**：
- `@RestController`、`@RequestMapping`、`@GetMapping`、`@PostMapping` 等
- `@Valid`、`@Validated`（参数校验）
- `@PathVariable`、`@RequestParam`、`@RequestBody`（参数绑定）

**禁止事项**：
- ❌ 禁止直接注入 `Repository` 接口
- ❌ 禁止在 controller 中编写业务逻辑（if/else 业务判断）
- ❌ 禁止直接操作数据库（SQL 语句、JPA 操作）
- ❌ 禁止在 controller 中处理事务（`@Transactional`）

### service 层（业务逻辑层）

**职责**：
- 实现核心业务逻辑
- 编排多个 repository 操作（事务管理）
- 执行业务规则校验
- 将 domain 实体转换为 DTO（或使用 MapStruct）
- 处理业务异常

**允许的注解**：
- `@Service`（Spring 组件注解）
- `@Transactional`（事务管理）
- `@Autowired` 或构造器注入

**禁止事项**：
- ❌ 禁止直接处理 HTTP 请求/响应（`HttpServletRequest`、`ResponseEntity`）
- ❌ 禁止返回 HTTP 状态码
- ❌ 禁止包含 Spring MVC 相关注解（`@RequestMapping` 等）

### domain 层（领域层）

**职责**：
- 定义领域实体（JPA Entity 或 MyBatis 映射对象）
- 定义数据传输对象（DTO、Request、Response）
- 定义枚举类型
- 定义值对象（Value Object）

**允许的注解**：
- JPA 注解：`@Entity`、`@Table`、`@Column`、`@Id` 等
- Lombok 注解：`@Data`、`@Builder`、`@NoArgsConstructor` 等
- 校验注解：`@NotNull`、`@Size`、`@Email` 等（用于 DTO）

**禁止事项**：
- ❌ 禁止包含 Spring 业务注解（`@Service`、`@Repository`、`@Component`）
- ❌ 禁止注入其他 Spring Bean
- ❌ 禁止包含业务逻辑方法（domain 对象应为纯数据容器）
- ❌ 禁止直接依赖 controller、service 或 repository 层的类

### repository 层（数据访问层）

**职责**：
- 定义数据访问接口（JPA Repository 或 MyBatis Mapper）
- 实现数据库 CRUD 操作
- 定义自定义查询方法

**允许的注解**：
- `@Repository`（Spring 组件注解）
- JPA：`@Query`、`@Modifying` 等
- MyBatis：`@Mapper`、`@Select`、`@Insert` 等

**禁止事项**：
- ❌ 禁止包含业务逻辑
- ❌ 禁止直接被 controller 调用
- ❌ 禁止返回 HTTP 相关对象

---

## 辅助包说明

除四层架构外，还有以下辅助包：

### config 包

**职责**：Spring 配置类（`@Configuration`）、安全配置、数据源配置等

**规则**：
- 可以依赖任何层的类（用于配置 Bean）
- 不应包含业务逻辑

### common 包

**职责**：公共工具类、常量定义、通用枚举

**规则**：
- ✅ 可以被任何层使用
- ❌ 禁止依赖 controller、service、domain、repository 层的类
- ❌ 禁止包含 Spring 业务注解

### exception 包

**职责**：自定义异常类、全局异常处理器（`@RestControllerAdvice`）

**规则**：
- 自定义异常类：不依赖任何层
- 全局异常处理器：可以依赖 domain 层（用于构建错误响应 DTO）
- ❌ 禁止在异常类中包含业务逻辑

---

## 包结构约定

```
{basePackage}/
├── config/          # Spring 配置类
│   └── package-info.java
├── common/          # 公共工具、常量
│   └── package-info.java
├── exception/       # 异常定义与处理
│   └── package-info.java
├── controller/      # REST 控制器
│   └── package-info.java
├── service/         # 业务逻辑
│   └── package-info.java
├── domain/          # 领域模型、DTO
│   └── package-info.java
└── repository/      # 数据访问层
    └── package-info.java
```

每个包必须包含 `package-info.java` 文件，说明该包的职责边界。

---

## 禁止的跨层依赖

以下依赖关系是**严格禁止**的，Codex 在生成代码时会拒绝创建这类依赖：

| 禁止的依赖 | 违规示例 | 正确做法 |
|-----------|---------|---------|
| controller → repository | `@Autowired UserRepository userRepo;`（在 Controller 中） | 通过 service 层间接访问 |
| domain → service | `@Autowired UserService service;`（在 Entity 中） | domain 应为纯 POJO |
| domain → repository | `@Autowired UserRepository repo;`（在 Entity 中） | domain 应为纯 POJO |
| repository → service | `@Autowired UserService service;`（在 Repository 中） | repository 只做数据访问 |
| common → 业务层 | `@Autowired UserService service;`（在 Utils 中） | common 应为无状态工具类 |

---

## 违规示例与正确示例

### 示例 1：controller 直接调用 repository（违规）

```java
// ❌ 违规：controller 直接注入并调用 repository
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;  // ❌ 禁止！

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id)  // ❌ 禁止！
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

```java
// ✅ 正确：controller 通过 service 访问数据
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;  // ✅ 注入 service

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        UserDTO user = userService.findById(id);  // ✅ 通过 service
        return ResponseEntity.ok(user);
    }
}
```

### 示例 2：domain 对象包含 Spring 注解（违规）

```java
// ❌ 违规：domain 实体包含 Spring 业务注解
@Entity
@Service  // ❌ 禁止！Entity 不应有 @Service
public class User {

    @Autowired  // ❌ 禁止！Entity 不应注入 Bean
    private UserRepository userRepository;

    public void save() {
        userRepository.save(this);  // ❌ 禁止！
    }
}
```

```java
// ✅ 正确：domain 实体为纯 POJO
@Entity
@Table(name = "t_user")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false)
    private String email;
}
```

### 示例 3：service 返回 HTTP 响应（违规）

```java
// ❌ 违规：service 返回 ResponseEntity
@Service
public class UserService {

    public ResponseEntity<UserDTO> findById(Long id) {  // ❌ 禁止！
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Not found"));
        return ResponseEntity.ok(new UserDTO(user));  // ❌ 禁止！
    }
}
```

```java
// ✅ 正确：service 返回业务对象，由 controller 处理 HTTP 响应
@Service
public class UserService {

    public UserDTO findById(Long id) {  // ✅ 返回业务 DTO
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));  // ✅ 抛出业务异常
        return UserDTO.from(user);
    }
}
```
