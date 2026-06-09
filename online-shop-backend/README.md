# 电商购物平台后端

## 启动

```powershell
mvn spring-boot:run
```

默认使用 SQLite，数据库文件为 `online-shop.db`。切换 MySQL：

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## 验证

```powershell
mvn test
mvn clean verify -Pharness-new
```
