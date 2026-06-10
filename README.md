# Online Shop Platform

一个前后端分离的在线商城示例项目。后端使用 Spring Boot、MyBatis Plus 和 SQLite/MySQL，前端使用 Vue 3、Vite、Element Plus 和 Axios，实现商品浏览、分页筛选、购物车、下单、订单查询与取消等核心购物流程。

## 主要功能

- 商品列表：支持按商品名称、分类、价格区间分页查询商品，每次切换页码或筛选条件都会发起接口请求。
- 商品详情：查看商品价格、库存、描述等信息。
- 用户登录：演示账号登录后获取 Bearer Token，购物车和订单接口基于 token 识别当前用户。
- 购物车：加入商品、修改数量、删除商品、查看购物车总价。
- 订单管理：从购物车提交订单，查看订单列表和订单详情，取消订单并回补库存。
- 前后端联调：Vite 代理 `/api` 到后端服务，开发时无需单独处理跨域。
- 一键启动：根目录 `start.bat` 会启动前后端服务，并自动打开前端首页和后端商品接口。

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
- JUnit、JaCoCo、Checkstyle、SpotBugs

### 前端

- Vue 3
- Vite 5
- Vue Router
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
│       ├── schema-sqlite.sql         # SQLite 表结构
│       └── data-sqlite.sql           # SQLite 初始化数据
├── online-shop-frontend/             # Vue 前端应用
│   ├── src
│   │   ├── request.js                # Axios 请求实例、拦截器和错误处理
│   │   ├── api.js                    # 业务 API 封装
│   │   ├── router.js                 # 页面路由
│   │   ├── views/                    # 商品、购物车、结算、订单页面
│   │   └── styles.css                # 全局样式
│   └── vite.config.js                # Vite 开发服务和 API 代理
├── harness-collab/                   # 项目需求、设计、API 与协作文档
├── config/                           # Checkstyle、SpotBugs 配置
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
| 商品 | `GET` | `/api/products` | 分页查询商品列表，支持名称、分类、价格区间筛选 |
| 商品 | `GET` | `/api/products/{id}` | 查询商品详情 |
| 购物车 | `GET` | `/api/cart` | 查询当前用户购物车 |
| 购物车 | `POST` | `/api/cart/items` | 加入购物车 |
| 购物车 | `PUT` | `/api/cart/items/{id}` | 修改购物车商品数量 |
| 购物车 | `DELETE` | `/api/cart/items/{id}` | 删除购物车商品 |
| 订单 | `POST` | `/api/orders` | 提交订单 |
| 订单 | `GET` | `/api/orders` | 查询订单列表 |
| 订单 | `GET` | `/api/orders/{id}` | 查询订单详情 |
| 订单 | `PUT` | `/api/orders/{id}/cancel` | 取消订单 |

购物车和订单接口需要在请求头中携带：

```http
Authorization: Bearer <token>
```

演示账号：

```text
用户名：demo
密码：demo123
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

商品分页响应位于统一响应的 `data` 字段下，包含 `items`、`total`、`page`、`pageSize` 和 `totalPages`。

## 页面路由

| 路径 | 页面 |
|------|------|
| `/login` | 用户登录 |
| `/products` | 商品列表 |
| `/products/:id` | 商品详情 |
| `/cart` | 购物车，需登录 |
| `/checkout` | 结算，需登录 |
| `/orders` | 订单列表，需登录 |
| `/orders/:id` | 订单详情，需登录 |

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

- 关闭旧的前后端启动窗口。
- 清理 `8080` 和 `5173` 端口上的旧进程。
- 启动后端：`http://localhost:8080`
- 启动前端：`http://localhost:5173`
- 自动打开后端商品接口：`http://localhost:8080/api/products`
- 自动打开前端页面：`http://localhost:5173`

### 手动启动后端

```powershell
cd online-shop-backend
mvn spring-boot:run -Dspring-boot.run.profiles=sqlite
```

默认端口为 `8080`。SQLite 会使用项目本地数据库文件，初始化脚本位于 `src/main/resources/schema-sqlite.sql` 和 `src/main/resources/data-sqlite.sql`。

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

前端构建：

```powershell
cd online-shop-frontend
npm install
npm run build
```

## 许可证

本项目使用 MIT License，详见 [LICENSE](LICENSE)。
