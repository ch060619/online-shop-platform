---
codex-harness: required
scope: workspace
---

# 测试与质量规范

本文件是 Codex Harness 的必读 guidance，规定测试分层策略、命名规范和质量要求，对工作区内所有 Spring Boot 子项目强制生效。

---

## 一、测试分层策略

### 1.1 Service 层单元测试

使用 `@ExtendWith(MockitoExtension.class)` 进行纯单元测试，隔离外部依赖。

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void should_returnUser_when_userExists() {
        // given
        Long userId = 1L;
        User mockUser = new User(userId, "张三");
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // when
        UserDTO result = userService.getUserById(userId);

        // then
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("张三");
    }
}
```

**要求**：
- 所有外部依赖（Repository、外部服务）必须使用 Mock
- 每个业务方法至少覆盖正常路径和异常路径
- 不得启动 Spring 上下文

### 1.2 Controller 层切片测试

使用 `@WebMvcTest` 进行 Controller 层切片测试，只加载 Web 层相关组件。

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void should_return200_when_userExists() throws Exception {
        // given
        UserDTO mockUser = new UserDTO(1L, "张三");
        when(userService.getUserById(1L)).thenReturn(mockUser);

        // when & then
        mockMvc.perform(get("/api/users/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("张三"));
    }
}
```

**要求**：
- 使用 `@MockBean` 模拟 Service 层
- 验证 HTTP 状态码、响应体格式、请求参数校验
- 不得直接调用 Service 的真实实现

### 1.3 Repository 层切片测试

根据持久化框架选择对应注解：

- **JPA 项目**：使用 `@DataJpaTest`
- **MyBatis 项目**：使用 `@MybatisTest`（需引入 `mybatis-spring-boot-test-autoconfigure`）

```java
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void should_findUser_when_emailExists() {
        // given
        User user = new User("张三", "zhangsan@example.com");
        userRepository.save(user);

        // when
        Optional<User> result = userRepository.findByEmail("zhangsan@example.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("张三");
    }
}
```

**要求**：
- 使用内嵌数据库（H2）或 Testcontainers
- 测试自定义查询方法的正确性
- 每次测试后数据自动回滚（`@Transactional` 默认行为）

### 1.4 集成测试

使用 `@SpringBootTest` 进行全链路集成测试，验证组件协作。

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void should_createAndRetrieveUser_when_validRequest() {
        // 完整的创建 → 查询流程验证
    }
}
```

**要求**：
- 集成测试放置在独立的测试类中，不与单元测试混用
- 使用 `application-test.yml` 配置测试环境
- 集成测试数量应少于单元测试，避免测试套件过慢

---

## 二、测试方法命名规范

**所有测试方法必须遵循以下命名格式：**

```
should_[预期行为]_when_[条件]
```

示例：

| 测试场景 | 方法名 |
|----------|--------|
| 用户存在时返回用户信息 | `should_returnUser_when_userExists` |
| 用户不存在时抛出异常 | `should_throwException_when_userNotFound` |
| 请求参数为空时返回 400 | `should_return400_when_requestBodyIsNull` |
| 邮箱重复时创建失败 | `should_failCreation_when_emailAlreadyExists` |
| 有效 Token 时通过认证 | `should_passAuthentication_when_tokenIsValid` |

---

## 三、测试类与被测类的对应关系

### 3.1 包路径对应

测试类必须与被测类保持**相同的包路径**，位于 `src/test/java` 下：

```
src/main/java/com/example/myapp/service/UserService.java
    ↓ 对应
src/test/java/com/example/myapp/service/UserServiceTest.java

src/main/java/com/example/myapp/controller/UserController.java
    ↓ 对应
src/test/java/com/example/myapp/controller/UserControllerTest.java
```

### 3.2 测试类覆盖要求

**每个业务类必须有对应的测试类**，包括：
- 所有 `@Service` 类
- 所有 `@RestController` 类
- 所有 `@Repository` 接口（自定义查询方法）
- 所有工具类（`common` 包下）

---

## 四、往返属性测试（Round-Trip Property Test）

**解析器、序列化器、格式转换器必须编写往返属性测试**，验证 `parse → format → parse` 结果等价。

要求：
- 使用 jqwik 或 JUnit 5 参数化测试实现
- 生成随机输入，验证往返一致性
- 覆盖边界值：空字符串、特殊字符、最大长度

示例（使用 jqwik）：

```java
@Property(tries = 100)
void should_preserveValue_when_serializeAndDeserialize(
        @ForAll @StringLength(min = 1, max = 100) String originalValue) {
    // given: 序列化
    String serialized = mySerializer.serialize(originalValue);

    // when: 反序列化
    String deserialized = mySerializer.deserialize(serialized);

    // then: 往返结果等价
    assertThat(deserialized).isEqualTo(originalValue);
}
```

---

## 五、测试覆盖率要求

| 模式 | 覆盖率要求 | 验证命令 |
|------|-----------|----------|
| `harness-new`（新项目） | 行覆盖率 ≥ 80% | `mvn clean verify -Pharness-new` |
| `harness-legacy`（历史项目） | 仅生成报告，无阈值 | `mvn clean verify -Pharness-legacy` |

覆盖率报告路径：`target/site/jacoco/index.html`

---

## 六、禁止事项

| 禁止行为 | 替代方案 |
|----------|----------|
| 在测试中使用 `Thread.sleep` | 使用 `Awaitility` 等待异步操作完成 |
| 测试依赖执行顺序 | 每个测试独立，使用 `@BeforeEach` 初始化状态 |
| 在测试中硬编码生产环境配置 | 使用 `application-test.yml` 和 `@ActiveProfiles("test")` |
| 跳过失败的测试（`@Disabled`）而不说明原因 | 必须在 `@Disabled` 注解中注明原因和计划修复时间 |

Awaitility 使用示例：

```java
// 禁止
Thread.sleep(2000);

// 正确
await().atMost(5, SECONDS)
       .until(() -> messageQueue.size() > 0);
```

---

## 七、Codex 执行约束

1. **生成业务代码时**：必须同步生成对应的测试类，测试类包路径与被测类一致
2. **生成测试方法时**：方法名必须遵循 `should_[预期行为]_when_[条件]` 格式
3. **生成解析器/序列化器时**：必须同步生成往返属性测试
4. **提示覆盖率验证**：在完成编码阶段后，提示开发者执行 `mvn clean verify -Pharness-new` 验证覆盖率
