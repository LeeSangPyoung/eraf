# PostgreSQL SQL 실행 스크립트
Write-Host "===== Executing SQL scripts =====" -ForegroundColor Cyan

# PostgreSQL 경로 찾기
$psqlPaths = @(
    "C:\Program Files\PostgreSQL\16\bin\psql.exe",
    "C:\Program Files\PostgreSQL\15\bin\psql.exe",
    "C:\Program Files\PostgreSQL\14\bin\psql.exe",
    "C:\PostgreSQL\16\bin\psql.exe",
    "C:\PostgreSQL\15\bin\psql.exe"
)

$psql = $null
foreach ($path in $psqlPaths) {
    if (Test-Path $path) {
        $psql = $path
        break
    }
}

if (-not $psql) {
    Write-Host "psql.exe not found. Please install PostgreSQL or add it to PATH." -ForegroundColor Red
    exit 1
}

Write-Host "Found psql at: $psql" -ForegroundColor Green

# Set environment variable for password
$env:PGPASSWORD = "eraf123"

Write-Host ""
Write-Host "[1/2] Creating gateway_consumers table and sample data..." -ForegroundColor Yellow
& $psql -U eraf -d eraf_gateway -f "consumers-init.sql"

Write-Host ""
Write-Host "[2/2] Registering Admin APIs (Consumers + Health Checks)..." -ForegroundColor Yellow
& $psql -U eraf -d eraf_gateway -f "admin-apis-init.sql"

Write-Host ""
Write-Host "===== SQL scripts completed =====" -ForegroundColor Green
