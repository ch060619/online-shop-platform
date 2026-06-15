# 电商购物平台部署说明

**最后更新**：2026-06-15

## 本地 Docker Compose

```powershell
docker compose up --build
```

服务端口：

| 服务 | 地址 |
|------|------|
| 后端 API | `http://localhost:8080` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| MySQL | `localhost:3306` |
| Redis | `localhost:6379` |
| RabbitMQ | `localhost:5672` |
| RabbitMQ Management | `http://localhost:15672` |

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `SPRING_PROFILES_ACTIVE` | `docker` | 后端容器使用 Docker profile |
| `MYSQL_HOST` | `mysql` | MySQL 服务名或地址 |
| `MYSQL_PORT` | `3306` | MySQL 端口 |
| `MYSQL_DATABASE` | `online_shop` | 数据库名 |
| `MYSQL_USERNAME` | `root` | 数据库用户 |
| `MYSQL_PASSWORD` | `root` | 数据库密码 |
| `REDIS_HOST` | `redis` | Redis 服务名或地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `RABBITMQ_HOST` | `rabbitmq` | RabbitMQ 服务名或地址 |
| `RABBITMQ_PORT` | `5672` | RabbitMQ 端口 |
| `SHOP_AUTH_TOKEN_SECRET` | `change-me-in-real-deployments` | JWT 签名密钥，真实环境必须替换 |

## 验证命令

```powershell
docker compose config
docker compose up --build
curl http://localhost:8080/v3/api-docs
```

前端仍按现有 Vite 流程构建：

```powershell
Set-Location online-shop-frontend
npm run build
```
