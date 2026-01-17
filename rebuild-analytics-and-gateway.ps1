# Analytics 모듈 재빌드 후 Gateway 전체 빌드
$ErrorActionPreference = "Stop"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Step 1: Build analytics feature module" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

Set-Location "$PSScriptRoot\eraf-api-gateway\eraf-gateway-feature-analytics"

Write-Host "Running: mvn clean install -DskipTests" -ForegroundColor Yellow
& mvn clean install -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "[FAILED] Analytics module build failed" -ForegroundColor Red
    exit 1
}

Write-Host "[SUCCESS] Analytics module built and installed" -ForegroundColor Green
Write-Host ""

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Step 2: Build gateway-builder (FULL)" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

Set-Location "$PSScriptRoot\eraf-api-gateway\eraf-gateway-builder"

Write-Host "Running: mvn clean package -P full -DskipTests" -ForegroundColor Yellow
& mvn clean package -P full -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "[FAILED] Gateway build failed" -ForegroundColor Red
    exit 1
}

$jarFile = Get-Item "target\eraf-gateway-1.0.0-SNAPSHOT.jar" -ErrorAction SilentlyContinue
if ($jarFile) {
    $sizeMB = [math]::Round($jarFile.Length / 1MB, 2)
    Write-Host "[SUCCESS] Gateway built successfully" -ForegroundColor Green
    Write-Host "JAR Size: $sizeMB MB" -ForegroundColor Green
    Write-Host "JAR Path: $($jarFile.FullName)" -ForegroundColor Gray
} else {
    Write-Host "[FAILED] JAR file not found" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Step 3: Starting Gateway..." -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Gateway will start on http://localhost:8080" -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop" -ForegroundColor Gray
Write-Host ""

& java -jar $jarFile.FullName
