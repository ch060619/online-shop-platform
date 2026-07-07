# 电商购物平台深挖增强执行计划

**关联需求**：[电商购物平台深挖增强需求](../01-product-specs/online-shop-platform-enhancement-spec.md)  
**关联设计**：[电商购物平台深挖增强设计](../02-design-docs/online-shop-platform-enhancement-design.md)  
**文档状态**：已交付  
**创建时间**：2026-06-15  
**最后更新**：2026-07-07
**负责人**：@dev

---

## 实施步骤

1. 文档门禁阶段：补增强需求、设计、执行计划和 `func.md`，创建本地分支并提交 `docs: add enhancement spec and design`。
2. Redis 商品缓存阶段：实现商品列表/详情 Cache-Aside、空值缓存、TTL 随机化、热点预热和命中率指标。
3. Redis+Lua 订单幂等阶段：`POST /api/orders` 增加 `Idempotency-Key`，Lua 原子处理 `PROCESSING / SUCCESS / FAILED`。
4. RabbitMQ 订单超时与状态机阶段：扩展 `CREATED / PAID / CANCELLED / TIMEOUT`，新增支付接口和 TTL/DLX 延迟消息超时处理。
5. 鉴权安全阶段：BCrypt、access token、refresh token、USER/ADMIN 权限隔离。
6. 压测与 SQL 优化阶段：补 JMeter/Gatling 脚本、EXPLAIN 记录和实测指标。
7. 部署与文档阶段：补 Dockerfile、Docker Compose、`application-docker.yml`、SpringDoc/OpenAPI、错误码和核心流程图。

每个阶段完成后必须先通过两轮质量检查，再执行本地 Git commit；不执行 `git push`。

---

## 测试执行记录

| 执行时间 | 执行命令 | 结果 | 行覆盖率 | 分支覆盖率 | 备注 |
|----------|----------|------|---------|-----------|------|
| 2026-07-07 | `mvn test`（online-shop-backend） | 通过 | 不适用 | 不适用 | 注册、购物车选中、地址簿、个人中心定向与回归检查，146 个测试通过 |
| 2026-07-07 | `npm run build`（online-shop-frontend） | 通过 | 不适用 | 不适用 | 前端生产构建通过；Vite 输出 chunk size 警告，不阻断构建 |
| 2026-07-07 | `mvn clean verify -Pharness-new`（online-shop-backend） | 通过 | 89.12% | 10.95% | 146 个测试通过，Checkstyle 0，SpotBugs 0，JaCoCo 覆盖率门禁通过 |
| 2026-07-07 | `cmd /c start.bat local` + HTTP 检查 | 通过 | 不适用 | 不适用 | 8080/5173 均监听，`/api/products` 与前端首页均返回 200；本机 Docker daemon 未运行，Redis/RabbitMQ 端口未就绪但脚本按设计继续启动 |
| 2026-06-15 | 文档自检：需求、设计、执行计划、func.md 字段与链接核对 | 通过 | 不适用 | 不适用 | 文档门禁阶段定向检查通过 |
| 2026-06-15 | `mvn clean verify -Pharness-new` | 通过 | 89.71% | 6.74% | 后端 64 个测试通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-15 | `mvn clean test jacoco:report jacoco:check@jacoco-check` | 通过 | 89.71% | 6.74% | 显式 JaCoCo 校验通过，方法覆盖率 69.59% |
| 2026-06-15 | `mvn "-Dtest=ProductServiceImplTest,RedisProductCacheServiceTest" test` | 通过 | 不适用 | 不适用 | Redis 商品缓存定向检查，22 个测试通过 |
| 2026-06-15 | `mvn clean verify -Pharness-new` | 通过 | 89.39% | 9.22% | Redis 阶段全量门禁，77 个测试通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-15 | `mvn clean test jacoco:report jacoco:check@jacoco-check` | 通过 | 89.39% | 9.22% | Redis 阶段显式 JaCoCo 校验通过，方法覆盖率 71.11% |
| 2026-06-15 | `mvn "-Dtest=OrderServiceImplTest,OrderControllerTest,RedisOrderIdempotencyServiceTest,OrderIdempotencyConcurrencyTest" test` | 通过 | 不适用 | 不适用 | 订单幂等定向检查，32 个测试通过 |
| 2026-06-15 | `mvn clean verify -Pharness-new` | 通过 | 89.34% | 10.21% | 订单幂等全量门禁，90 个测试通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-15 | `mvn clean test jacoco:report jacoco:check@jacoco-check` | 通过 | 89.34% | 10.21% | 订单幂等显式 JaCoCo 校验通过，方法覆盖率 71.74% |
| 2026-06-15 | `mvn "-Dtest=OrderServiceImplTest,OrderControllerTest,OrderMapperTest,OrderTimeoutSchedulerTest,OrderIdempotencyConcurrencyTest" test` | 通过 | 不适用 | 不适用 | 订单状态机定向检查，37 个测试通过 |
| 2026-06-15 | `mvn clean verify -Pharness-new` | 通过 | 89.17% | 10.95% | 订单状态机全量门禁，99 个测试通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-15 | `mvn clean test jacoco:report jacoco:check@jacoco-check` | 通过 | 89.17% | 10.95% | 订单状态机显式 JaCoCo 校验通过，方法覆盖率 72.03% |
| 2026-06-15 | `mvn "-Dtest=OrderServiceImplTest,OrderTimeoutMessageListenerTest,RabbitOrderTimeoutMessagePublisherTest,OrderIdempotencyConcurrencyTest,OrderTimeoutSchedulerTest" test` | 通过 | 不适用 | 不适用 | RabbitMQ 订单超时定向检查，29 个测试通过 |
| 2026-06-15 | `mvn clean verify -Pharness-new` | 通过 | 88.02% | 10.74% | RabbitMQ 订单超时全量门禁，104 个测试通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-15 | `mvn clean test jacoco:report jacoco:check@jacoco-check` | 通过 | 88.02% | 10.74% | RabbitMQ 订单超时显式 JaCoCo 校验通过，方法覆盖率 70.67% |
| 2026-06-15 | `mvn "-Dtest=AuthServiceImplTest,AuthControllerTest,TokenServiceTest,CommonSupportTest,ProductControllerTest,CartControllerTest,OrderControllerTest,RefreshTokenMapperTest" test` | 通过 | 不适用 | 不适用 | 鉴权安全定向检查，45 个测试通过 |
| 2026-06-15 | `mvn clean verify -Pharness-new` | 通过 | 83.26% | 10.62% | 鉴权安全全量门禁，114 个测试通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-15 | `mvn clean test jacoco:report jacoco:check@jacoco-check` | 通过 | 83.26% | 10.62% | 鉴权安全显式 JaCoCo 校验通过，方法覆盖率 69.75% |
| 2026-06-15 | `mvn "-Dtest=ProductServiceImplTest,ProductControllerTest,PerformanceSchemaInitializerTest,SqlExplainPlanTest" test` | 通过 | 不适用 | 不适用 | 压测与 SQL 优化定向检查，34 个测试通过 |
| 2026-06-15 | `.\scripts\loadtest\run-jmeter.ps1 -BaseUrl http://localhost:18080 -Threads 5 -RampSeconds 5 -DurationSeconds 30` | 通过 | 不适用 | 不适用 | JMeter 实测 5003 samples，QPS 166.77，P95 143ms，P99 208ms，错误率 0%，缓存命中率 99.9% |
| 2026-06-15 | `mvn clean verify -Pharness-new` | 通过 | 88.72% | 11.79% | 压测与 SQL 优化全量门禁，124 个测试通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-15 | `mvn test jacoco:report jacoco:check@jacoco-check` | 通过 | 88.72% | 11.79% | 压测与 SQL 优化显式 JaCoCo 校验通过，方法覆盖率 74.61% |
| 2026-06-15 | 文档自检：Dockerfile、Compose、Docker profile、OpenAPI、错误码、核心流程图、func.md 链接核对 | 通过 | 不适用 | 不适用 | 部署与文档阶段定向检查通过 |
| 2026-06-15 | `mvn "-Dtest=OpenApiConfigTest,DockerProfileConfigTest" test` | 通过 | 不适用 | 不适用 | SpringDoc 与 Docker profile 定向检查，2 个测试通过 |
| 2026-06-15 | `docker compose config` | 通过 | 不适用 | 不适用 | Compose 配置解析通过，包含 MySQL、Redis、RabbitMQ、backend 服务 |
| 2026-06-15 | `npm run build` | 通过 | 不适用 | 不适用 | 前端生产构建通过；Vite 输出 chunk size 警告，不阻断构建 |
| 2026-06-15 | `mvn clean verify -Pharness-new` | 通过 | 88.84% | 11.79% | 部署与文档阶段全量门禁，126 个测试通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-15 | `mvn test jacoco:report jacoco:check@jacoco-check` | 通过 | 88.84% | 11.79% | 部署与文档阶段显式 JaCoCo 校验通过，方法覆盖率 74.68% |

### 覆盖率趋势

| 日期 | 行覆盖率 | 分支覆盖率 | 方法覆盖率 | 类覆盖率 |
|------|---------|-----------|-----------|---------|
| 2026-06-15 | 89.71% | 6.74% | 69.59% | — |
| 2026-06-15 Redis 商品缓存阶段 | 89.39% | 9.22% | 71.11% | — |
| 2026-06-15 订单幂等阶段 | 89.34% | 10.21% | 71.74% | — |
| 2026-06-15 订单状态机阶段 | 89.17% | 10.95% | 72.03% | — |
| 2026-06-15 RabbitMQ 订单超时阶段 | 88.02% | 10.74% | 70.67% | — |
| 2026-06-15 鉴权安全阶段 | 83.26% | 10.62% | 69.75% | — |
| 2026-06-15 压测与 SQL 优化阶段 | 88.72% | 11.79% | 74.61% | — |
| 2026-06-15 部署与文档阶段 | 88.84% | 11.79% | 74.68% | — |
| 2026-07-07 注册地址与选中结算阶段 | 89.12% | 10.95% | 74.67% | — |

**覆盖率报告路径**：`online-shop-backend/target/site/jacoco/index.html`

---

## 阶段检查清单

| 阶段 | 定向检查 | 全量检查 | Git 检查点 |
|------|----------|----------|------------|
| 文档门禁 | 文档字段完整、链接有效、`func.md` 已登记 | 后端门禁命令可通过 | `docs: add enhancement spec and design` |
| Redis 商品缓存 | 缓存组件和 ProductService 定向测试 | Maven 全量 + 显式 JaCoCo | `feat: add redis product cache` |
| Redis+Lua 幂等 | 幂等服务、Controller、并发测试 | Maven 全量 + 显式 JaCoCo | `feat: add redis lua order idempotency` |
| 订单状态机 | 状态机、支付、超时扫描、重复执行测试 | Maven 全量 + 显式 JaCoCo | `feat: add order state machine` |
| RabbitMQ 订单超时 | 延迟消息发布、消费、重复投递幂等测试 | Maven 全量 + 显式 JaCoCo | `feat: add rabbitmq order timeout` |
| 鉴权安全 | BCrypt、刷新令牌、权限测试 | Maven 全量 + 显式 JaCoCo | `feat: harden authentication and authorization` |
| 压测与 SQL | 脚本语法、EXPLAIN 记录、实测报告 | Maven 全量 + 压测命令记录 | `perf: add load tests and sql indexes` |
| 部署与文档 | Compose 配置、Swagger、文档核对 | Maven 全量 + 前端构建 + Compose 校验 | `chore: add docker compose and api docs` |

---

## 问题记录

| # | 问题描述 | 严重程度 | 发现时间 | 状态 | 解决方案 | 解决时间 |
|---|----------|----------|----------|------|----------|----------|
| 1 | 深挖增强功能尚未实现 | P2 | 2026-06-15 | 处理中 | 按阶段实现并在每阶段回填测试记录 | — |
| 2 | 非 ASCII 工作区路径下 JaCoCo 未稳定生成默认 `target/jacoco.exec`，导致显式覆盖率门禁无法读取执行数据 | P1 | 2026-06-15 | 已解决 | 在后端 `pom.xml` 配置 ASCII 路径 `C:/codex-jacoco/online-shop-backend.exec`，并补充 Surefire `argLine` 与 JaCoCo 插件配置 | 2026-06-15 |
| 3 | Redis 阶段首次全量门禁发现 `catch` 右花括号不符合严格 Checkstyle 风格 | P2 | 2026-06-15 | 已解决 | 将 `try` 结束花括号与 `catch` 拆成独立行，保持项目严格规则一致 | 2026-06-15 |
| 4 | 热点商品预热测试发现不存在商品会重复写入空值缓存 | P2 | 2026-06-15 | 已解决 | 保留 `getById` 内部空值缓存写入，预热 catch 只吞掉不存在商品异常 | 2026-06-15 |
| 5 | 订单幂等测试泛型 matcher 导致 Mockito `thenReturn` 编译失败 | P2 | 2026-06-15 | 已解决 | 将 RedisScript matcher 拆为 `RedisScript<List>` 和 `RedisScript<Long>` 两个强类型 helper | 2026-06-15 |
| 6 | 状态机 Mapper 测试暴露旧 SQLite 库缺少 `expire_at` 等新增列 | P2 | 2026-06-15 | 已解决 | 增加 `OrderSchemaInitializer` 做启动期增量补列和索引，Mapper 测试导入同一初始化器 | 2026-06-15 |
| 7 | 状态机首次全量门禁发现 2 处严格 Checkstyle 行长违规 | P2 | 2026-06-15 | 已解决 | 拆分建索引 SQL 和 MyBatis `typeHandler` 注解长字符串 | 2026-06-15 |
| 8 | RabbitMQ 发布失败可能在订单已提交后污染下单响应 | P2 | 2026-06-15 | 已解决 | 超时消息改为事务提交后发布，发布失败记录 warning 并依赖可关闭兜底扫描配置处理 | 2026-06-15 |
| 9 | 商品写接口权限隔离后，Controller 测试仍使用旧 USER token 路径 | P2 | 2026-06-15 | 已解决 | 测试中补 ADMIN token，并新增 USER 写商品返回 403 的权限断言 | 2026-06-15 |
| 10 | 旧 SQLite 本地库缺少 `user.role` 和 `refresh_token` 表，可能影响升级启动 | P2 | 2026-06-15 | 已解决 | 增加 `AuthSchemaInitializer` 做启动期补列、建表、索引和示例账号兼容初始化 | 2026-06-15 |
| 11 | 鉴权阶段首次全量门禁发现 `UserRole` 枚举常量缺少 Javadoc | P2 | 2026-06-15 | 已解决 | 为 USER / ADMIN 枚举常量补充说明，满足严格 Checkstyle | 2026-06-15 |
| 12 | 鉴权阶段 SpotBugs 发现 `AuthSchemaInitializer` 存在重复分支 | P2 | 2026-06-15 | 已解决 | 删除无效布尔分支，直接使用 `CURRENT_TIMESTAMP` 默认值 | 2026-06-15 |

---

## 静态检查结果

### Checkstyle 检查

| 执行时间 | Profile | 违规数量 | 状态 | 主要违规类型 |
|----------|---------|---------|------|-------------|
| 2026-06-15 | `harness-new` | 0 | 通过 | 无 |
| 2026-06-15 Redis 商品缓存阶段 | `harness-new` | 0 | 通过 | 无 |
| 2026-06-15 订单幂等阶段 | `harness-new` | 0 | 通过 | 无 |
| 2026-06-15 订单状态机阶段 | `harness-new` | 0 | 通过 | 无 |
| 2026-06-15 RabbitMQ 订单超时阶段 | `harness-new` | 0 | 通过 | 无 |
| 2026-06-15 鉴权安全阶段 | `harness-new` | 0 | 通过 | 无 |
| 2026-06-15 压测与 SQL 优化阶段 | `harness-new` | 0 | 通过 | 无 |
| 2026-06-15 部署与文档阶段 | `harness-new` | 0 | 通过 | 无 |
| 2026-07-07 注册地址与选中结算阶段 | `harness-new` | 0 | 通过 | 无 |

### SpotBugs 检查

| 执行时间 | Profile | Bug 数量 | 高危 Bug | 状态 |
|----------|---------|---------|---------|------|
| 2026-06-15 | `harness-new` | 0 | 0 | 通过 |
| 2026-06-15 Redis 商品缓存阶段 | `harness-new` | 0 | 0 | 通过 |
| 2026-06-15 订单幂等阶段 | `harness-new` | 0 | 0 | 通过 |
| 2026-06-15 订单状态机阶段 | `harness-new` | 0 | 0 | 通过 |
| 2026-06-15 RabbitMQ 订单超时阶段 | `harness-new` | 0 | 0 | 通过 |
| 2026-06-15 鉴权安全阶段 | `harness-new` | 0 | 0 | 通过 |
| 2026-06-15 压测与 SQL 优化阶段 | `harness-new` | 0 | 0 | 通过 |
| 2026-06-15 部署与文档阶段 | `harness-new` | 0 | 0 | 通过 |
| 2026-07-07 注册地址与选中结算阶段 | `harness-new` | 0 | 0 | 通过 |

---

## 压测指标记录

> 只有实际执行压测后才允许填写数字；禁止预填 QPS、P95、P99、错误率、缓存命中率。

| 场景 | 数据量 | 并发数 | QPS | P95 | P99 | 错误率 | 缓存命中率 | 备注 |
|------|--------|--------|-----|-----|-----|--------|------------|------|
| 商品列表-缓存后 | 商品 6 条 | 5 | 166.77 | 143ms | 208ms | 0% | 99.9% | JMeter 20260615-221845，`product-list` 场景 1000 samples，P95 5ms，P99 9ms |
| 商品详情-缓存后 | 商品 6 条 | 5 | 166.77 | 143ms | 208ms | 0% | 99.9% | JMeter 20260615-221845，`product-detail` 场景 1000 samples，P95 5ms，P99 8ms |
| 下单链路 | 商品 6 条，商品 1 库存预置 100000 | 5 | 166.77 | 143ms | 208ms | 0% | 不适用 | JMeter 20260615-221845，`order-create` 场景 999 samples，P95 50ms，P99 131ms |

**压测机器配置**：Intel(R) Core(TM) i7-14650HX，15.78GB RAM。  
**压测结果文件**：`load-test-results/20260615-221845/summary.md`（本地生成产物，不提交 Git）。  
**说明**：本阶段在 Redis 商品缓存已上线后执行压测，不补写缓存前数据，避免引入非实测对比数字。

---

## 发布检查清单

### 代码质量

- [x] 单元测试全部通过（`mvn test` 无失败）
- [x] 行覆盖率 >= 80%（显式 JaCoCo 校验通过）
- [x] Checkstyle 无违规（`mvn clean verify -Pharness-new` 通过）
- [x] SpotBugs 无高危 Bug（`mvn clean verify -Pharness-new` 通过）
- [x] 所有 P0/P1 问题已解决

### 代码规范

- [x] 所有公共方法包含 Javadoc 注释
- [x] 代码符合四层架构约束（无跨层直接依赖）
- [x] 测试类与被测类保持相同包路径
- [x] 测试方法命名遵循 `should_[预期行为]_when_[条件]` 格式

### 文档同步

- [x] API 文档已更新（`harness-collab/04-api-docs/` 对应文档）
- [x] `harness-collab/func.md` 功能状态已更新
- [x] 本执行计划文档已完整填写

### Git 检查点

- [x] 文档门禁阶段已本地 commit
- [x] Redis 商品缓存阶段已本地 commit
- [x] Redis+Lua 幂等阶段已本地 commit
- [x] 订单状态机阶段已本地 commit
- [x] RabbitMQ 订单超时阶段已本地 commit
- [x] 鉴权安全阶段已本地 commit
- [x] 压测与 SQL 优化阶段已本地 commit
- [x] 部署与文档阶段已本地 commit

---

## 发布摘要

**发布时间**：2026-06-15  
**发布版本**：深挖增强本地交付版  
**发布人**：@dev

**本次发布内容**：
- Redis 商品缓存、Redis+Lua 订单幂等、RabbitMQ 订单超时、订单状态机、鉴权安全、压测 SQL 优化、Docker Compose 和 OpenAPI 文档。

**影响范围**：
- 后端商品、订单、认证、安全、缓存、消息队列、部署和文档。
- 前端登录、下单、订单状态展示和管理权限入口。

**回滚方案**：
- 使用每阶段本地 Git commit 作为检查点，通过 `git log --oneline` 定位阶段提交后按需回退。
