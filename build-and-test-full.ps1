# eraf-commons full 빌드 및 eraf-api-gateway 통합 테스트
$ErrorActionPreference = "Stop"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Step 1: Build eraf-commons-aggregator (FULL)" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Set-Location "$PSScriptRoot\eraf-commons\eraf-commons-aggregator"

Write-Host "Running: mvn clean install -P full -DskipTests" -ForegroundColor Yellow
& mvn clean install -P full -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "[FAILED] eraf-commons-aggregator build failed" -ForegroundColor Red
    exit 1
}

$jarFile = Get-Item "target\eraf-commons-1.0.0-SNAPSHOT.jar" -ErrorAction SilentlyContinue
if ($jarFile) {
    $sizeKB = [math]::Round($jarFile.Length / 1KB, 2)
    Write-Host "[SUCCESS] eraf-commons-aggregator installed to local Maven repo" -ForegroundColor Green
    Write-Host "JAR Size: $sizeKB KB" -ForegroundColor Green
} else {
    Write-Host "[FAILED] JAR file not found" -ForegroundColor Red
    exit 1
}
Write-Host ""

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Step 2: Build eraf-api-gateway (FULL)" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Set-Location "$PSScriptRoot\eraf-api-gateway\eraf-gateway-builder"

Write-Host "Running: mvn clean package -P full -DskipTests" -ForegroundColor Yellow
& mvn clean package -P full -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "[FAILED] eraf-api-gateway build failed" -ForegroundColor Red
    exit 1
}

$jarFile = Get-Item "target\eraf-gateway-1.0.0-SNAPSHOT.jar" -ErrorAction SilentlyContinue
if ($jarFile) {
    $sizeMB = [math]::Round($jarFile.Length / 1MB, 2)
    Write-Host "[SUCCESS] eraf-api-gateway built" -ForegroundColor Green
    Write-Host "JAR Size: $sizeMB MB" -ForegroundColor Green
} else {
    Write-Host "[FAILED] JAR file not found" -ForegroundColor Red
    exit 1
}
Write-Host ""

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Step 3: Run eraf-api-gateway" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Starting gateway on http://localhost:8080" -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop" -ForegroundColor Gray
Write-Host ""

& java -jar target\eraf-gateway-1.0.0-SNAPSHOT.jar
