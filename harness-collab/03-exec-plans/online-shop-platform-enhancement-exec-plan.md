# 电商购物平台深挖增强执行计划

**关联需求**：[电商购物平台深挖增强需求](../01-product-specs/online-shop-platform-enhancement-spec.md)  
**关联设计**：[电商购物平台深挖增强设计](../02-design-docs/online-shop-platform-enhancement-design.md)  
**文档状态**：执行中  
**创建时间**：2026-06-15  
**最后更新**：2026-06-15  
**负责人**：@dev

---

## 实施步骤

1. 文档门禁阶段：补增强需求、设计、执行计划和 `func.md`，创建本地分支并提交 `docs: add enhancement spec and design`。
2. Redis 商品缓存阶段：实现商品列表/详情 Cache-Aside、空值缓存、TTL 随机化、热点预热和命中率指标。
3. Redis+Lua 订单幂等阶段：`POST /api/orders` 增加 `Idempotency-Key`，Lua 原子处理 `PROCESSING / SUCCESS / FAILED`。
4. RabbitMQ 订单超时与状态机阶段：扩展 `CREATED / PAID / CANCELLED / TIMEOUT`，新增支付接口和延迟消息超时处理。
5. 鉴权安全阶段：BCrypt、access token、refresh token、USER/ADMIN 权限隔离。
6. 压测与 SQL 优化阶段：补 JMeter/Gatling 脚本、EXPLAIN 记录和实测指标。
7. 部署与文档阶段：补 Dockerfile、Docker Compose、`application-docker.yml`、SpringDoc/OpenAPI、错误码和核心流程图。

每个阶段完成后必须先通过两轮质量检查，再执行本地 Git commit；不执行 `git push`。

---

## 测试执行记录

| 执行时间 | 执行命令 | 结果 | 行覆盖率 | 分支覆盖率 | 备注 |
|----------|----------|------|---------|-----------|------|
| 2026-06-15 | 文档自检：需求、设计、执行计划、func.md 字段与链接核对 | 通过 | 不适用 | 不适用 | 文档门禁阶段定向检查通过 |
| 2026-06-15 | `mvn clean verify -Pharness-new` | 通过 | 89.71% | 6.74% | 后端 64 个测试通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-15 | `mvn clean test jacoco:report jacoco:check@jacoco-check` | 通过 | 89.71% | 6.74% | 显式 JaCoCo 校验通过，方法覆盖率 69.59% |
| 2026-06-15 | `mvn "-Dtest=ProductServiceImplTest,RedisProductCacheServiceTest" test` | 通过 | 不适用 | 不适用 | Redis 商品缓存定向检查，22 个测试通过 |
| 2026-06-15 | `mvn clean verify -Pharness-new` | 通过 | 89.39% | 9.22% | Redis 阶段全量门禁，77 个测试通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-15 | `mvn clean test jacoco:report jacoco:check@jacoco-check` | 通过 | 89.39% | 9.22% | Redis 阶段显式 JaCoCo 校验通过，方法覆盖率 71.11% |
| 2026-06-15 | `mvn "-Dtest=OrderServiceImplTest,OrderControllerTest,RedisOrderIdempotencyServiceTest,OrderIdempotencyConcurrencyTest" test` | 通过 | 不适用 | 不适用 | 订单幂等定向检查，32 个测试通过 |
| 2026-06-15 | `mvn clean verify -Pharness-new` | 通过 | 89.34% | 10.21% | 订单幂等全量门禁，90 个测试通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-15 | `mvn clean test jacoco:report jacoco:check@jacoco-check` | 通过 | 89.34% | 10.21% | 订单幂等显式 JaCoCo 校验通过，方法覆盖率 71.74% |

### 覆盖率趋势

| 日期 | 行覆盖率 | 分支覆盖率 | 方法覆盖率 | 类覆盖率 |
|------|---------|-----------|-----------|---------|
| 2026-06-15 | 89.71% | 6.74% | 69.59% | — |
| 2026-06-15 Redis 商品缓存阶段 | 89.39% | 9.22% | 71.11% | — |
| 2026-06-15 订单幂等阶段 | 89.34% | 10.21% | 71.74% | — |

**覆盖率报告路径**：`online-shop-backend/target/site/jacoco/index.html`

---

## 阶段检查清单

| 阶段 | 定向检查 | 全量检查 | Git 检查点 |
|------|----------|----------|------------|
| 文档门禁 | 文档字段完整、链接有效、`func.md` 已登记 | 后端门禁命令可通过 | `docs: add enhancement spec and design` |
| Redis 商品缓存 | 缓存组件和 ProductService 定向测试 | Maven 全量 + 显式 JaCoCo | `feat: add redis product cache` |
| Redis+Lua 幂等 | 幂等服务、Controller、并发测试 | Maven 全量 + 显式 JaCoCo | `feat: add redis lua order idempotency` |
| RabbitMQ 状态机 | 状态机、支付、超时消费、重复消息测试 | Maven 全量 + 显式 JaCoCo | `feat: add rabbitmq order timeout` |
| 鉴权安全 | BCrypt、刷新令牌、权限测试 | Maven 全量 + 显式 JaCoCo + 前端构建 | `feat: harden authentication and authorization` |
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

---

## 静态检查结果

### Checkstyle 检查

| 执行时间 | Profile | 违规数量 | 状态 | 主要违规类型 |
|----------|---------|---------|------|-------------|
| 2026-06-15 | `harness-new` | 0 | 通过 | 无 |
| 2026-06-15 Redis 商品缓存阶段 | `harness-new` | 0 | 通过 | 无 |
| 2026-06-15 订单幂等阶段 | `harness-new` | 0 | 通过 | 无 |

### SpotBugs 检查

| 执行时间 | Profile | Bug 数量 | 高危 Bug | 状态 |
|----------|---------|---------|---------|------|
| 2026-06-15 | `harness-new` | 0 | 0 | 通过 |
| 2026-06-15 Redis 商品缓存阶段 | `harness-new` | 0 | 0 | 通过 |
| 2026-06-15 订单幂等阶段 | `harness-new` | 0 | 0 | 通过 |

---

## 压测指标记录

> 只有实际执行压测后才允许填写数字；禁止预填 QPS、P95、P99、错误率、缓存命中率。

| 场景 | 数据量 | 并发数 | QPS | P95 | P99 | 错误率 | 缓存命中率 | 备注 |
|------|--------|--------|-----|-----|-----|--------|------------|------|
| 商品列表-缓存前 | 待执行 | 待执行 | 待执行 | 待执行 | 待执行 | 待执行 | 不适用 | — |
| 商品列表-缓存后 | 待执行 | 待执行 | 待执行 | 待执行 | 待执行 | 待执行 | 待执行 | — |
| 商品详情-缓存前 | 待执行 | 待执行 | 待执行 | 待执行 | 待执行 | 待执行 | 不适用 | — |
| 商品详情-缓存后 | 待执行 | 待执行 | 待执行 | 待执行 | 待执行 | 待执行 | 待执行 | — |
| 下单链路 | 待执行 | 待执行 | 待执行 | 待执行 | 待执行 | 待执行 | 不适用 | 校验无重复订单 |

---

## 发布检查清单

### 代码质量

- [x] 单元测试全部通过（`mvn test` 无失败）
- [x] 行覆盖率 >= 80%（显式 JaCoCo 校验通过）
- [x] Checkstyle 无违规（`mvn clean verify -Pharness-new` 通过）
- [x] SpotBugs 无高危 Bug（`mvn clean verify -Pharness-new` 通过）
- [ ] 所有 P0/P1 问题已解决

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
- [ ] Redis+Lua 幂等阶段已本地 commit
- [ ] RabbitMQ 状态机阶段已本地 commit
- [ ] 鉴权安全阶段已本地 commit
- [ ] 压测与 SQL 优化阶段已本地 commit
- [ ] 部署与文档阶段已本地 commit

---

## 发布摘要

**发布时间**：待定  
**发布版本**：待定  
**发布人**：@dev

**本次发布内容**：
- 待所有阶段完成后回填。

**影响范围**：
- 后端商品、订单、认证、安全、缓存、消息队列、部署和文档。
- 前端登录、下单、订单状态展示和管理权限入口。

**回滚方案**：
- 使用每阶段本地 Git commit 作为检查点，通过 `git log --oneline` 定位阶段提交后按需回退。
