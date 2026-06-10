# 电商购物平台执行计划

**关联需求**：[电商购物平台需求](../01-product-specs/online-shop-platform-spec.md)  
**关联设计**：[电商购物平台设计](../02-design-docs/online-shop-platform-design.md)  
**创建时间**：2026-06-09  
**最后更新**：2026-06-10  
**负责人**：@dev

---

## 实施步骤

1. 创建后端 Maven 子项目和前端 Vite 子项目。
2. 实现后端公共响应、异常处理、请求日志拦截器、CORS、Shiro 基础配置和数据库初始化。
3. 实现商品、购物车、订单的 domain、repository、service、controller。
4. 同步创建核心 Service 和 Controller 测试。
5. 实现前端路由、Axios 封装和六个核心页面。
6. 执行 Maven 与前端构建验证，记录结果。
7. 同步 API 文档和功能资产总表。

---

## 测试执行记录

| 执行时间 | 执行命令 | 结果 | 行覆盖率 | 分支覆盖率 | 备注 |
|----------|----------|------|---------|-----------|------|
| 2026-06-09 19:25 | `mvn test` | 通过 | — | — | 后端 29 个测试通过 |
| 2026-06-09 19:13 | `npm run build` | 通过 | — | — | 前端 Vite 构建通过，存在 chunk 体积提示 |
| 2026-06-09 19:23 | `mvn clean verify -Pharness-new` | 通过 | 86.11% | 3.66% | 后端 29 个测试通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-09 22:52 | `mvn clean verify -Pharness-new` | 通过 | 88.71% | 5.13% | 后端 48 个测试通过；Spring Boot 3 + MyBatis Plus 订单 CRUD 验证通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-10 19:21 | `mvn test` | 通过 | — | — | 后端 49 个测试通过，商品分页 service/controller 测试通过 |
| 2026-06-10 19:29 | `mvn clean verify -Pharness-new` | 通过 | 89.30% | 5.22% | 后端 50 个测试通过，商品分页 Mapper slice 测试通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-10 19:28 | `npm run build` | 通过 | — | — | 前端异步请求封装与商品分页筛选构建通过，存在 chunk 体积提示 |
| 2026-06-10 20:00 | `npm run build` | 通过 | — | — | 商品详情促销标签和倒计时子组件构建通过，存在 chunk 体积提示 |
| 2026-06-10 22:00 | `npm run build` | 通过 | — | — | Pinia 购物车状态、清空购物车、下单成功页和 404 路由构建通过，存在 chunk 体积提示 |
| 2026-06-10 20:48 | `mvn clean verify -Pharness-new` | 通过 | 89.73% | 6.60% | 后端 62 个测试通过；商品 add/delete/update/query、统一分页响应和全局异常处理验证通过，Checkstyle 0，SpotBugs 0 |
| 2026-06-10 20:49 | `product-crud-postman-collection.json` | 已编写 | — | — | Postman/ApiFox 可导入集合覆盖商品 CRUD、参数错误、业务异常和接口不存在场景 |
| 2026-06-10 21:19 | `mvn test -Dtest=OrderServiceImplTest` | 通过 | — | — | 订单服务 11 个测试通过，新增创建订单时写入系统时间断言 |
| 2026-06-10 21:20 | `mvn clean verify -Pharness-new` | 通过 | — | — | 后端 62 个测试通过，Checkstyle 0，SpotBugs 0；本次命令输出中 JaCoCo 执行数据被跳过，随后单独补跑覆盖率校验 |
| 2026-06-10 21:21 | `mvn test jacoco:report` + `jacoco:check@jacoco-check` | 通过 | 89.77% | 6.60% | 显式 JaCoCo agent 生成覆盖率报告，profile 覆盖率阈值检查通过 |
| 2026-06-10 21:36 | `mvn test -Dtest=OrderMapperTest` | 通过 | — | — | 订单 Mapper 2 个测试通过，覆盖 SQLite `created_at` 写入格式和历史 ISO 分隔符读回 |
| 2026-06-10 21:37 | `mvn test -Dtest=OrderServiceImplTest,OrderMapperTest` | 通过 | — | — | 订单服务和 Mapper 共 13 个测试通过 |
| 2026-06-10 21:38 | `mvn clean verify -Pharness-new` | 通过 | — | — | 后端 64 个测试通过，Checkstyle 0，SpotBugs 0；本次命令输出中 JaCoCo 执行数据被跳过，随后单独补跑覆盖率校验 |
| 2026-06-10 21:39 | `mvn test jacoco:report` + `jacoco:check@jacoco-check` | 通过 | 89.71% | 6.74% | 显式 JaCoCo agent 生成覆盖率报告，profile 覆盖率阈值检查通过 |

### 覆盖率趋势

| 日期 | 行覆盖率 | 分支覆盖率 | 方法覆盖率 | 类覆盖率 |
|------|---------|-----------|-----------|---------|
| 2026-06-09 | 86.11% | 3.66% | — | — |
| 2026-06-09 | 88.71% | 5.13% | 67.61% | 100% |
| 2026-06-10 | 89.30% | 5.22% | 68.66% | 100% |
| 2026-06-10 | 89.73% | 6.60% | 69.15% | 100% |
| 2026-06-10 | 89.77% | 6.60% | 69.36% | 100% |
| 2026-06-10 | 89.71% | 6.74% | 69.59% | 100% |

**覆盖率报告路径**：`online-shop-backend/target/site/jacoco/index.html`

---

## 问题记录

| # | 问题描述 | 严重程度 | 发现时间 | 状态 | 解决方案 | 解决时间 |
|---|----------|----------|----------|------|----------|----------|
| 1 | 尚未执行测试验证 | P2 | 2026-06-09 | 已解决 | 已执行后端与前端验证并回填结果 | 2026-06-09 |
| 2 | Spring Boot 3 启动时 Shiro Boot 自动配置加载旧 `javax.servlet.Filter` 类型，导致 `start.bat` 后端启动失败 | P1 | 2026-06-09 | 已解决 | 移除 Shiro Boot starter，保留 `shiro-spring:jakarta` 与 `shiro-web:jakarta`，由项目现有拦截器负责 API 登录态校验 | 2026-06-09 |
| 3 | 商品维护接口、统一分页响应和异常场景缺少覆盖测试记录 | P2 | 2026-06-10 | 已解决 | 新增 controller/service/mapper 自动化测试，并在 API 文档补充 Postman/ApiFox 全覆盖场景清单 | 2026-06-10 |
| 4 | SQLite 默认 `CURRENT_TIMESTAMP` 与应用系统时区不一致，订单创建时间显示偏离系统时间 | P1 | 2026-06-10 | 已解决 | 创建订单时由 `OrderServiceImpl` 显式写入 `LocalDateTime.now()`，并补充单元测试断言 | 2026-06-10 |
| 5 | SQLite 读取订单 `created_at` 时无法解析 ISO `T` 分隔时间，导致订单确认页报 timestamp parsing 错误 | P1 | 2026-06-10 | 已解决 | 新增 SQLite 兼容的 `LocalDateTime` TypeHandler，订单 Mapper 显式写入空格分隔时间并兼容读回历史 ISO 格式 | 2026-06-10 |

---

## 静态检查结果

### Checkstyle 检查

| 执行时间 | Profile | 违规数量 | 状态 | 主要违规类型 |
|----------|---------|---------|------|-------------|
| 2026-06-09 19:19 | harness-new | 0 | 通过 | — |
| 2026-06-09 22:52 | harness-new | 0 | 通过 | — |
| 2026-06-10 20:48 | harness-new | 0 | 通过 | — |
| 2026-06-10 21:20 | harness-new | 0 | 通过 | — |
| 2026-06-10 21:38 | harness-new | 0 | 通过 | — |

### SpotBugs 检查

| 执行时间 | Profile | Bug 数量 | 高危 Bug | 状态 |
|----------|---------|---------|---------|------|
| 2026-06-09 19:19 | harness-new | 0 | 0 | 通过 |
| 2026-06-09 22:52 | harness-new | 0 | 0 | 通过 |
| 2026-06-10 20:48 | harness-new | 0 | 0 | 通过 |
| 2026-06-10 21:20 | harness-new | 0 | 0 | 通过 |
| 2026-06-10 21:38 | harness-new | 0 | 0 | 通过 |

---

## 发布检查清单

### 代码质量

- [x] 单元测试全部通过（`mvn test` 无失败）
- [x] 行覆盖率 ≥ 80%（`mvn clean verify -Pharness-new` 通过）
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
- [x] 本执行计划文档已创建

### CI/CD

- [ ] PR 已创建
- [ ] CI 流水线已通过（GitHub Actions 全绿）
- [ ] 代码已通过 Code Review

---

## 发布摘要

**发布时间**：2026-06-09 19:19  
**发布版本**：v1.0.0  
**发布人**：@dev

**本次发布内容**：
- 电商购物平台后端 API。
- 电商购物平台 Vue 前端页面。
- Harness 文档和 API 文档同步。

**影响范围**：
- 新增实验项目，不影响现有 Harness 模板。

**回滚方案**：
- 删除 `online-shop-backend` 与 `online-shop-frontend` 子项目，并移除相关协作文档记录。
