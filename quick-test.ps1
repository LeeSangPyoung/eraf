# Quick Test - Consumers & Health Checks API
# Usage: .\quick-test.ps1 -Token "<YOUR_JWT_TOKEN>"

param(
    [Parameter(Mandatory=$true)]
    [string]$Token
)

$BaseUrl = "http://localhost:9000"
$Headers = @{
    "Authorization" = "Bearer $Token"
}

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  Quick Test - Consumers & Health Checks" -ForegroundColor White
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Get all consumers
Write-Host "[1/6] GET /admin/consumers" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/admin/consumers" -Headers $Headers
    Write-Host "✅ Success! Found $($response.Count) consumers" -ForegroundColor Green
    $response | Select-Object -First 3 | Format-Table id, username, apiKey, rateLimit, enabled
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 2: Get consumer stats
Write-Host "[2/6] GET /admin/consumers/stats" -ForegroundColor Yellow
try {
    $stats = Invoke-RestMethod -Uri "$BaseUrl/admin/consumers/stats" -Headers $Headers
    Write-Host "✅ Success!" -ForegroundColor Green
    $stats | Format-List
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 3: Create new consumer
Write-Host "[3/6] POST /admin/consumers (Create new)" -ForegroundColor Yellow
$newConsumer = @{
    username = "quick-test-consumer"
    description = "Quick Test Consumer"
    rateLimit = 300
    enabled = $true
} | ConvertTo-Json

try {
    $created = Invoke-RestMethod -Uri "$BaseUrl/admin/consumers" -Method POST -Headers (@{
        "Authorization" = "Bearer $Token"
        "Content-Type" = "application/json"
    }) -Body $newConsumer
    Write-Host "✅ Success! Created consumer ID: $($created.id)" -ForegroundColor Green
    Write-Host "   API Key: $($created.apiKey)" -ForegroundColor Cyan
    $consumerId = $created.id
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
    $consumerId = $null
}
Write-Host ""

# Test 4: Get health checks
Write-Host "[4/6] GET /admin/health-checks" -ForegroundColor Yellow
try {
    $healthChecks = Invoke-RestMethod -Uri "$BaseUrl/admin/health-checks" -Headers $Headers
    Write-Host "✅ Success! Found $($healthChecks.Count) targets" -ForegroundColor Green
    $healthChecks | Format-Table targetId, serviceName, host, port, healthStatus, enabled
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 5: Get health check stats
Write-Host "[5/6] GET /admin/health-checks/stats" -ForegroundColor Yellow
try {
    $healthStats = Invoke-RestMethod -Uri "$BaseUrl/admin/health-checks/stats" -Headers $Headers
    Write-Host "✅ Success!" -ForegroundColor Green
    $healthStats | Format-List
} catch {
    Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 6: Delete test consumer (cleanup)
if ($consumerId) {
    Write-Host "[6/6] DELETE /admin/consumers/$consumerId (Cleanup)" -ForegroundColor Yellow
    try {
        Invoke-RestMethod -Uri "$BaseUrl/admin/consumers/$consumerId" -Method DELETE -Headers $Headers
        Write-Host "✅ Success! Test consumer deleted" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "[6/6] SKIPPED (no consumer to delete)" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  Quick Test Completed!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
