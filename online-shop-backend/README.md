# 电商购物平台后端

## 启动

```powershell
mvn spring-boot:run
```

默认使用 SQLite，数据库文件为 `online-shop.db`。切换 MySQL：

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Docker profile 由根目录 `docker-compose.yml` 使用：

```powershell
docker compose up --build
```

## OpenAPI

启动后端后访问：

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## 验证

```powershell
mvn test
mvn clean verify -Pharness-new
```
