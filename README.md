# Online Shop Platform

一个前后端分离的在线商城示例项目。后端使用 Spring Boot、MyBatis Plus 和 SQLite/MySQL，前端使用 Vue 3、Vite、Element Plus 和 Axios，实现商品浏览、分页筛选、购物车、下单、订单查询与取消等核心购物流程。

## V2.0 发布要点

- 认证增强：登录响应新增 `accessToken`、`refreshToken`、过期时间和用户角色，支持刷新令牌轮换。
- 权限控制：商品写接口仅允许 ADMIN 角色访问，购物车和订单接口要求 USER 或 ADMIN 登录。
- 订单增强：订单提交支持 `Idempotency-Key` 幂等键，新增状态机、支付、更新、删除和超时关闭流程。
- 缓存与消息：商品查询接入 Redis 缓存并提供缓存指标接口；订单超时关闭接入 RabbitMQ TTL/DLX。
- 部署支持：新增 Dockerfile、`docker-compose.yml`、Docker profile、OpenAPI/Swagger UI 和压测脚本。

## 主要功能

- 商品列表：支持按商品名称、分类、价格区间分页查询商品，每次切换页码或筛选条件都会发起接口请求，统一响应包含分页元信息。
- 商品缓存：商品列表和详情查询支持 Redis 缓存，提供缓存命中率指标接口。
- 商品维护：后端提供商品新增、删除、更新和查询基础接口，覆盖正常参数、异常参数和商品不存在场景。
- 商品详情：查看商品价格、库存、描述等信息，并展示父组件传入子组件的促销标签和限时倒计时。
- 用户登录：演示账号登录后获取 Bearer Token 与刷新令牌，购物车和订单接口基于 token 识别当前用户。
- 购物车：通过 Pinia 维护全局购物车状态，支持加入商品、修改数量、删除商品、清空购物车和实时计算总价。
- 订单管理：从购物车幂等提交订单，订单创建时间由后端应用使用系统时间写入，并以 SQLite/MySQL 可读的时间格式持久化；下单成功后跳转成功页，可继续查看订单列表和订单详情，支持支付、更新、取消、删除和超时关闭。
- 路由兜底：前端提供 404 页面，未知地址会跳转到友好的页面不存在提示。
- 全局异常：后端统一处理参数错误、业务异常、接口不存在和未预期异常，所有接口保持统一 JSON 结构。
- 前后端联调：Vite 代理 `/api` 到后端服务，开发时无需单独处理跨域。
- 一键启动：根目录 `start.bat` 支持本地开发模式和 Docker 模式，并自动打开前端首页和后端商品接口。

## 技术栈

### 后端

- Java 17
- Spring Boot 3.2.x
- Spring MVC
- MyBatis Plus
- Jakarta Validation
- Apache Shiro Jakarta
- Druid
- SQLite JDBC / MySQL Connector
- Redis
- RabbitMQ
- SpringDoc OpenAPI
- JUnit、JaCoCo、Checkstyle、SpotBugs

### 前端

- Vue 3
- Vite 5
- Vue Router
- Pinia
- Axios
- Element Plus
- lucide-vue-next

## 项目架构

```text
.
├── online-shop-backend/              # Spring Boot 后端服务
│   ├── src/main/java/com/example/shop
│   │   ├── common/                   # 通用响应、订单状态、Token 服务、当前用户上下文
│   │   ├── config/                   # Shiro、Web MVC 等配置
│   │   ├── controller/               # REST API 控制器
│   │   ├── domain/                   # DTO、Entity、VO
│   │   ├── exception/                # 业务异常和全局异常处理
│   │   ├── interceptor/              # Token 认证、请求日志拦截器
│   │   ├── repository/mapper/        # MyBatis Mapper
│   │   └── service/                  # 业务服务接口与实现
│   └── src/main/resources
│       ├── application.yml           # 默认配置
│       ├── application-sqlite.yml    # SQLite 数据源配置
│       ├── application-mysql.yml     # MySQL 数据源配置
│       ├── application-docker.yml    # Docker Compose 部署配置
│       ├── schema-sqlite.sql         # SQLite 表结构
│       └── data-sqlite.sql           # SQLite 初始化数据
│   └── Dockerfile                    # 后端容器镜像构建
├── online-shop-frontend/             # Vue 前端应用
│   ├── src
│   │   ├── request.js                # Axios 请求实例、拦截器和错误处理
│   │   ├── api.js                    # 业务 API 封装
│   │   ├── router.js                 # 页面路由
│   │   ├── stores/                   # Pinia 全局状态，如购物车状态
│   │   ├── components/               # 前端复用组件，如促销标签和倒计时
│   │   ├── views/                    # 商品、购物车、结算、订单页面
│   │   └── styles.css                # 全局样式
│   └── vite.config.js                # Vite 开发服务和 API 代理
├── harness-collab/                   # 项目需求、设计、API 与协作文档
├── config/                           # Checkstyle、SpotBugs 配置
├── docker-compose.yml                # MySQL、Redis、RabbitMQ、后端编排
├── start.bat                         # Windows 一键启动脚本
└── pom.xml                           # Harness 构建与质量门禁配置
```

后端采用典型分层架构：

- `controller`：提供 HTTP API，负责请求参数校验和响应封装。
- `service`：承载商品、购物车、订单等业务规则。
- `repository/mapper`：通过 MyBatis 访问数据库。
- `domain`：定义请求 DTO、数据库实体和前端响应 VO。
- `exception`、`common`、`config`：提供统一异常、响应格式、用户上下文和基础配置。

## API 概览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | `POST` | `/api/auth/login` | 用户登录，返回 Bearer Token |
| 认证 | `POST` | `/api/auth/refresh` | 使用刷新令牌轮换新的访问令牌 |
| 商品 | `GET` | `/api/products` | 分页查询商品列表，支持名称、分类、价格区间筛选 |
| 商品 | `GET` | `/api/products/{id}` | 查询商品详情 |
| 商品 | `GET` | `/api/products/cache/metrics` | 查询商品缓存命中率指标 |
| 商品 | `POST` | `/api/products/add` | 新增商品 |
| 商品 | `DELETE` | `/api/products/delete/{id}` | 删除商品 |
| 商品 | `PUT` | `/api/products/update/{id}` | 更新商品 |
| 商品 | `GET` | `/api/products/query` | 商品分页查询兼容入口 |
| 购物车 | `GET` | `/api/cart` | 查询当前用户购物车 |
| 购物车 | `POST` | `/api/cart/items` | 加入购物车 |
| 购物车 | `PUT` | `/api/cart/items/{id}` | 修改购物车商品数量 |
| 购物车 | `DELETE` | `/api/cart/items/{id}` | 删除购物车商品 |
| 订单 | `POST` | `/api/orders` | 提交订单 |
| 订单 | `GET` | `/api/orders` | 查询订单列表 |
| 订单 | `GET` | `/api/orders/{id}` | 查询订单详情 |
| 订单 | `PUT` | `/api/orders/{id}` | 更新订单收货信息 |
| 订单 | `PUT` | `/api/orders/{id}/pay` | 模拟支付订单 |
| 订单 | `PUT` | `/api/orders/{id}/cancel` | 取消订单 |
| 订单 | `DELETE` | `/api/orders/{id}` | 删除已取消订单 |

OpenAPI 文档：

| 类型 | 路径 |
|------|------|
| OpenAPI JSON | `/v3/api-docs` |
| Swagger UI | `/swagger-ui.html` |

商品新增、更新、删除接口需要 ADMIN 角色；购物车和订单接口需要 USER 或 ADMIN 角色。受保护接口需要在请求头中携带：

```http
Authorization: Bearer <token>
```

演示账号：

```text
用户名：demo
密码：demo123
角色：USER

用户名：admin
密码：admin123
角色：ADMIN
```

商品列表接口常用查询参数：

| 参数 | 说明 |
|------|------|
| `name` | 商品名称模糊搜索 |
| `category` | 商品分类 |
| `minPrice` | 最低价格 |
| `maxPrice` | 最高价格 |
| `page` | 页码，默认 `1` |
| `pageSize` | 每页数量，默认 `6`，最大 `50` |

统一响应结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "page": null
}
```

商品分页响应会在 `data` 字段下保留 `items`、`total`、`page`、`pageSize` 和 `totalPages`，同时在顶层 `page` 字段返回分页元信息。

商品新增/更新请求体示例：

```json
{
  "name": "机械键盘",
  "category": "数码配件",
  "price": 299.00,
  "stock": 10,
  "imageUrl": "image",
  "description": "desc"
}
```

接口异常统一返回 `code` 和 `message`：参数错误为 `400`，业务不存在为 `404`，未登录为 `401`，未预期错误为 `500`。Postman/ApiFox 覆盖测试清单见 `harness-collab/04-api-docs/online-shop-api.md`，可导入集合位于 `harness-collab/03-exec-plans/product-crud-postman-collection.json`。

## 页面路由

| 路径 | 页面 |
|------|------|
| `/login` | 用户登录 |
| `/products` | 商品列表 |
| `/products/:id` | 商品详情 |
| `/cart` | 购物车，需登录 |
| `/checkout` | 结算，需登录 |
| `/order-success/:id` | 下单成功，需登录 |
| `/orders` | 订单列表，需登录 |
| `/orders/:id` | 订单详情，需登录 |
| `/404` | 404 页面 |
| `/:pathMatch(.*)*` | 未知路由跳转到 404 页面 |

## 环境要求

- JDK 17
- Maven 3.6+
- Node.js 18+ 和 npm
- Windows 用户可直接使用根目录 `start.bat`

## 启动方式

### 一键启动

在项目根目录执行：

```powershell
.\start.bat
```

脚本会：

- 使用 SQLite 启动本地后端。
- 尝试通过 Docker 启动 Redis 和 RabbitMQ；如果未安装 Docker，请先自行启动本地 Redis `6379` 和 RabbitMQ `5672`。
- 关闭旧的前后端启动窗口。
- 清理 `8080` 和 `5173` 端口上的旧进程。
- 启动后端：`http://localhost:8080`
- 启动前端：`http://localhost:5173`
- 自动打开后端商品接口：`http://localhost:8080/api/products`
- 自动打开前端页面：`http://localhost:5173`

### Docker 模式启动

在项目根目录执行：

```powershell
.\start.bat docker
```

脚本会通过 `docker compose up --build -d` 启动 MySQL、Redis、RabbitMQ 和后端服务，然后启动前端开发服务。RabbitMQ 管理台地址为 `http://localhost:15672`，默认账号密码为 `guest` / `guest`。

### 手动启动后端

```powershell
cd online-shop-backend
mvn spring-boot:run -Dspring-boot.run.profiles=sqlite
```

默认端口为 `8080`。SQLite 会使用项目本地数据库文件，初始化脚本位于 `src/main/resources/schema-sqlite.sql` 和 `src/main/resources/data-sqlite.sql`。V2.0 本地模式还需要 Redis `6379` 和 RabbitMQ `5672`。

如需使用 MySQL：

```powershell
cd online-shop-backend
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

启动前请根据本地数据库环境调整 `online-shop-backend/src/main/resources/application-mysql.yml`。

### 手动启动前端

```powershell
cd online-shop-frontend
npm install
npm run dev -- --host 0.0.0.0 --port 5173
```

前端开发服务默认访问地址为 `http://localhost:5173`，`/api` 请求会代理到 `http://localhost:8080`。

### Docker Compose 启动后端依赖

```powershell
docker compose up --build
```

Compose 会启动 MySQL、Redis、RabbitMQ 和后端服务。部署变量和端口说明见 `harness-collab/04-api-docs/deployment.md`。

## 构建与测试

后端测试：

```powershell
cd online-shop-backend
mvn test
```

后端质量检查：

```powershell
cd online-shop-backend
mvn clean verify -Pharness-new
```

最近一次后端验证结果记录见 `harness-collab/03-exec-plans/online-shop-platform-enhancement-exec-plan.md`。

前端构建：

```powershell
cd online-shop-frontend
npm install
npm run build
```

## 许可证

本项目使用 MIT License，详见 [LICENSE](LICENSE)。
