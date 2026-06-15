# Online Shop Load Test

本目录保存 Online Shop Platform 的本地压测脚本。JMeter 二进制放在仓库根目录 `.local-tools/`，该目录被 `.gitignore` 忽略，不提交到 Git。

## 工具安装

当前本机已安装并校验：

- JMeter: `.local-tools/apache-jmeter-5.6.3/bin/jmeter.bat`
- 下载源：`https://mirrors.aliyun.com/apache/jmeter/binaries/apache-jmeter-5.6.3.zip`
- SHA-512 校验源：`https://downloads.apache.org/jmeter/binaries/apache-jmeter-5.6.3.zip.sha512`

重新安装时可执行：

```powershell
New-Item -ItemType Directory -Force -Path .local-tools | Out-Null
curl.exe -L -o .local-tools\apache-jmeter-5.6.3.zip https://mirrors.aliyun.com/apache/jmeter/binaries/apache-jmeter-5.6.3.zip
curl.exe -L -o .local-tools\apache-jmeter-5.6.3.zip.sha512 https://downloads.apache.org/jmeter/binaries/apache-jmeter-5.6.3.zip.sha512
$expected = ((Get-Content .local-tools\apache-jmeter-5.6.3.zip.sha512 | Select-Object -First 1) -split '\s+')[0].ToUpperInvariant()
$actual = (Get-FileHash -Algorithm SHA512 .local-tools\apache-jmeter-5.6.3.zip).Hash.ToUpperInvariant()
if ($expected -ne $actual) { throw "SHA512 mismatch" }
Expand-Archive .local-tools\apache-jmeter-5.6.3.zip .local-tools -Force
```

## 运行压测

先启动后端：

```powershell
Set-Location online-shop-backend
mvn spring-boot:run "-Dspring-boot.run.profiles=sqlite"
```

下单链路依赖 Redis+Lua 幂等，请确认 Redis 6379 已启动。本机已检测到 `redis-server`，可执行：

```powershell
redis-server --port 6379
```

再在仓库根目录执行轻量压测：

```powershell
.\scripts\loadtest\run-jmeter.ps1 -BaseUrl http://localhost:8080 -Threads 5 -RampSeconds 5 -DurationSeconds 30
```

脚本默认会用 `admin/admin123` 登录并把 `ProductId` 对应商品库存准备为 `100000`，避免短压测期间因库存耗尽造成业务错误。若不希望准备数据，可加 `-PrepareData $false`。

结果输出到 `load-test-results/{timestamp}/`：

- `online-shop.jtl`: 原始 JMeter 采样数据
- `html/`: JMeter HTML 报告
- `summary.md`: 机器配置、商品数据量、QPS、P95、P99、错误率、缓存命中率摘要

脚本会在压测前后读取 `/api/products/cache/metrics` 计算本轮商品详情和列表缓存命中率；若指标接口不可用，脚本会直接失败，避免生成缺少关键指标的报告。

## 场景覆盖

- `auth-login`: 登录获取 access token
- `product-list`: 商品列表查询
- `product-detail`: 商品详情查询
- `cart-add`: 加购物车
- `order-create`: 下单链路

压测指标必须来自实际 `summary.md`，禁止手填或预估。
