param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$Threads = 5,
    [int]$RampSeconds = 5,
    [int]$DurationSeconds = 30,
    [string]$Username = "demo",
    [string]$Password = "demo123",
    [string]$AdminUsername = "admin",
    [string]$AdminPassword = "admin123",
    [int]$ProductId = 1,
    [string]$ProductCategory = "数码配件",
    [string]$ResultDir = "load-test-results",
    [bool]$PrepareData = $true
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$jmeter = Join-Path $root ".local-tools\apache-jmeter-5.6.3\bin\jmeter.bat"
$plan = Join-Path $PSScriptRoot "online-shop-load-test.jmx"
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outputDir = Join-Path $root $ResultDir
$runDir = Join-Path $outputDir $timestamp
$jtl = Join-Path $runDir "online-shop.jtl"
$htmlReport = Join-Path $runDir "html"
$summary = Join-Path $runDir "summary.md"

if (!(Test-Path $jmeter)) {
    throw "JMeter not found at $jmeter. Download and extract apache-jmeter-5.6.3 under .local-tools first."
}
if (!(Test-Path $plan)) {
    throw "JMeter plan not found at $plan."
}

$uri = [Uri]$BaseUrl
$protocol = $uri.Scheme
$hostName = $uri.Host
$port = if ($uri.IsDefaultPort) { if ($protocol -eq "https") { 443 } else { 80 } } else { $uri.Port }

New-Item -ItemType Directory -Force -Path $runDir | Out-Null

$machineCpu = (Get-CimInstance Win32_Processor | Select-Object -First 1).Name
$machineMemoryGb = [Math]::Round(((Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory / 1GB), 2)
$productPage = Invoke-RestMethod `
    -Method Get `
    -Uri "$BaseUrl/api/products?page=1&pageSize=1"
$productTotal = if ($null -ne $productPage.page -and $null -ne $productPage.page.total) {
    $productPage.page.total
}
else {
    "unknown"
}

function Get-CacheMetrics {
    $metrics = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/products/cache/metrics"
    if ($metrics.code -ne 200 -or $null -eq $metrics.data) {
        throw "Product cache metrics endpoint is unavailable at $BaseUrl/api/products/cache/metrics."
    }
    return $metrics
}

function Get-DeltaRate {
    param(
        [long]$BeforeHits,
        [long]$BeforeMisses,
        [long]$AfterHits,
        [long]$AfterMisses
    )

    $hits = $AfterHits - $BeforeHits
    $misses = $AfterMisses - $BeforeMisses
    $total = $hits + $misses
    if ($total -le 0) {
        return "n/a"
    }
    return "$([Math]::Round(($hits * 100.0) / $total, 2))%"
}

if ($PrepareData) {
    $adminLogin = Invoke-RestMethod `
        -Method Post `
        -Uri "$BaseUrl/api/auth/login" `
        -ContentType "application/json" `
        -Body (@{ username = $AdminUsername; password = $AdminPassword } | ConvertTo-Json -Compress)
    if ($adminLogin.code -ne 200 -or [string]::IsNullOrWhiteSpace($adminLogin.data.accessToken)) {
        throw "Admin login failed while preparing load-test data."
    }
    $adminHeaders = @{ Authorization = "Bearer $($adminLogin.data.accessToken)" }
    $productBody = @{
        name = "机械键盘 K87"
        category = $ProductCategory
        price = 299.00
        stock = 100000
        imageUrl = "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=800&q=80"
        description = "Load-test prepared product stock."
    } | ConvertTo-Json -Compress
    $prepareResult = Invoke-RestMethod `
        -Method Put `
        -Uri "$BaseUrl/api/products/update/$ProductId" `
        -Headers $adminHeaders `
        -ContentType "application/json" `
        -Body $productBody
    if ($prepareResult.code -ne 200) {
        throw "Product stock preparation failed for product $ProductId."
    }
}

$cacheBefore = Get-CacheMetrics

& $jmeter `
    -n `
    -t $plan `
    -l $jtl `
    -e `
    -o $htmlReport `
    "-Jprotocol=$protocol" `
    "-Jhost=$hostName" `
    "-Jport=$port" `
    "-Jthreads=$Threads" `
    "-JrampSeconds=$RampSeconds" `
    "-JdurationSeconds=$DurationSeconds" `
    "-Jusername=$Username" `
    "-Jpassword=$Password" `
    "-JproductId=$ProductId" `
    "-JproductCategory=$ProductCategory" `
    "-JresultFile=$jtl"

if ($LASTEXITCODE -ne 0) {
    throw "JMeter exited with code $LASTEXITCODE."
}

$cacheAfter = Get-CacheMetrics

$rows = Import-Csv -LiteralPath $jtl
$total = $rows.Count
if ($total -eq 0) {
    throw "No JMeter samples were written to $jtl."
}

$elapsedSeconds = [Math]::Max(1, $DurationSeconds)
$success = @($rows | Where-Object { $_.success -eq "true" }).Count
$errors = $total - $success
$errorRate = [Math]::Round(($errors * 100.0) / $total, 2)
$qps = [Math]::Round($total / $elapsedSeconds, 2)
$elapsedValues = @($rows | ForEach-Object { [int]$_.elapsed } | Sort-Object)
$p95Index = [Math]::Min($elapsedValues.Count - 1, [Math]::Ceiling($elapsedValues.Count * 0.95) - 1)
$p99Index = [Math]::Min($elapsedValues.Count - 1, [Math]::Ceiling($elapsedValues.Count * 0.99) - 1)
$p95 = $elapsedValues[$p95Index]
$p99 = $elapsedValues[$p99Index]
$detailHitRate = Get-DeltaRate `
    -BeforeHits $cacheBefore.data.detailHits `
    -BeforeMisses $cacheBefore.data.detailMisses `
    -AfterHits $cacheAfter.data.detailHits `
    -AfterMisses $cacheAfter.data.detailMisses
$listHitRate = Get-DeltaRate `
    -BeforeHits $cacheBefore.data.listHits `
    -BeforeMisses $cacheBefore.data.listMisses `
    -AfterHits $cacheAfter.data.listHits `
    -AfterMisses $cacheAfter.data.listMisses

$byLabel = $rows | Group-Object label | Sort-Object Name | ForEach-Object {
    $labelRows = $_.Group
    $labelElapsed = @($labelRows | ForEach-Object { [int]$_.elapsed } | Sort-Object)
    $labelP95Index = [Math]::Min($labelElapsed.Count - 1, [Math]::Ceiling($labelElapsed.Count * 0.95) - 1)
    $labelP99Index = [Math]::Min($labelElapsed.Count - 1, [Math]::Ceiling($labelElapsed.Count * 0.99) - 1)
    $labelErrors = @($labelRows | Where-Object { $_.success -ne "true" }).Count
    "| $($_.Name) | $($labelRows.Count) | $($labelElapsed[$labelP95Index]) | $($labelElapsed[$labelP99Index]) | $labelErrors |"
}

@"
# Online Shop JMeter Load Test Summary

**Run time**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**Base URL**: $BaseUrl
**Threads**: $Threads
**Ramp seconds**: $RampSeconds
**Duration seconds**: $DurationSeconds
**Machine CPU**: $machineCpu
**Machine memory GB**: $machineMemoryGb
**Product rows**: $productTotal
**Prepared product id**: $ProductId
**Result file**: $jtl
**HTML report**: $htmlReport

| Samples | QPS | P95 ms | P99 ms | Error rate | Detail cache hit rate | List cache hit rate |
|---------|-----|--------|--------|------------|-----------------------|---------------------|
| $total | $qps | $p95 | $p99 | $errorRate% | $detailHitRate | $listHitRate |

| Label | Samples | P95 ms | P99 ms | Errors |
|-------|---------|--------|--------|--------|
$($byLabel -join "`n")
"@ | Set-Content -LiteralPath $summary -Encoding UTF8

Get-Content -LiteralPath $summary
