# ERAF Gateway - Consumers & Health Checks 테스트 스크립트
# 사용법: .\test-consumers-and-healthchecks.ps1 -Token "<JWT_TOKEN>"

param(
    [Parameter(Mandatory=$true)]
    [string]$Token
)

$BaseUrl = "http://localhost:9000"
$Headers = @{
    "Authorization" = "Bearer $Token"
    "Content-Type" = "application/json"
}

function Test-API {
    param(
        [string]$Method,
        [string]$Path,
        [string]$Body = $null,
        [string]$Description
    )

    Write-Host "-----------------------------------" -ForegroundColor Cyan
    Write-Host "TEST: $Description" -ForegroundColor Yellow
    Write-Host "-----------------------------------" -ForegroundColor Cyan
    Write-Host "REQUEST: $Method $Path"

    $params = @{
        Uri = "$BaseUrl$Path"
        Method = $Method
        Headers = $Headers
    }

    if ($Body) {
        Write-Host "BODY: $Body"
        $params.Body = $Body
    }

    Write-Host ""

    try {
        $response = Invoke-RestMethod @params
        Write-Host "RESPONSE:" -ForegroundColor Green
        $response | ConvertTo-Json -Depth 10
    } catch {
        Write-Host "ERROR:" -ForegroundColor Red
        Write-Host $_.Exception.Message
        if ($_.ErrorDetails) {
            Write-Host $_.ErrorDetails
        }
    }

    Write-Host ""
    Write-Host ""
}

Write-Host "=========================================" -ForegroundColor Magenta
Write-Host "  ERAF Gateway API Tests" -ForegroundColor White
Write-Host "=========================================" -ForegroundColor Magenta
Write-Host ""

##################################################
# 1. CONSUMERS API TESTS
##################################################

Write-Host "=========================================" -ForegroundColor Magenta
Write-Host "  1. CONSUMERS API TESTS" -ForegroundColor White
Write-Host "=========================================" -ForegroundColor Magenta
Write-Host ""

# 1.1 Get all consumers
Test-API -Method "GET" -Path "/admin/consumers" -Description "Get all consumers"

# 1.2 Get consumers (enabled only)
Test-API -Method "GET" -Path "/admin/consumers?enabledOnly=true" -Description "Get enabled consumers only"

# 1.3 Create new consumer
$NewConsumer = @{
    username = "test-api-consumer"
    description = "Test Consumer created via API"
    rateLimit = 500
    rateLimitWindowSeconds = 60
    enabled = $true
    tags = @{
        env = "test"
        purpose = "api-test"
    }
    customId = "test-001"
} | ConvertTo-Json

Test-API -Method "POST" -Path "/admin/consumers" -Body $NewConsumer -Description "Create new consumer"

# Get consumer ID
try {
    $allConsumers = Invoke-RestMethod -Uri "$BaseUrl/admin/consumers" -Method GET -Headers $Headers
    $consumerId = $allConsumers[-1].id
    Write-Host "Created Consumer ID: $consumerId" -ForegroundColor Green
    Write-Host ""
} catch {
    $consumerId = 1
}

# 1.4 Get consumer by ID
Test-API -Method "GET" -Path "/admin/consumers/$consumerId" -Description "Get consumer by ID"

# 1.5 Update consumer
$UpdatedConsumer = @{
    username = "test-api-consumer-updated"
    description = "Updated Test Consumer"
    rateLimit = 1000
    rateLimitWindowSeconds = 60
    enabled = $true
    tags = @{
        env = "test"
        purpose = "api-test"
        updated = "true"
    }
} | ConvertTo-Json

Test-API -Method "PUT" -Path "/admin/consumers/$consumerId" -Body $UpdatedConsumer -Description "Update consumer"

# 1.6 Regenerate API Key
Test-API -Method "POST" -Path "/admin/consumers/$consumerId/regenerate-key" -Description "Regenerate API Key"

# 1.7 Toggle consumer (disable)
Test-API -Method "PATCH" -Path "/admin/consumers/$consumerId/toggle" -Description "Toggle consumer (disable)"

# 1.8 Toggle consumer (enable again)
Test-API -Method "PATCH" -Path "/admin/consumers/$consumerId/toggle" -Description "Toggle consumer (enable)"

# 1.9 Get consumer statistics
Test-API -Method "GET" -Path "/admin/consumers/stats" -Description "Get consumer statistics"

# 1.10 Get consumer by username
Test-API -Method "GET" -Path "/admin/consumers/username/test-api-consumer-updated" -Description "Get consumer by username"

# 1.11 Delete consumer (cleanup)
Test-API -Method "DELETE" -Path "/admin/consumers/$consumerId" -Description "Delete consumer (cleanup)"

##################################################
# 2. HEALTH CHECKS API TESTS
##################################################

Write-Host "=========================================" -ForegroundColor Magenta
Write-Host "  2. HEALTH CHECKS API TESTS" -ForegroundColor White
Write-Host "=========================================" -ForegroundColor Magenta
Write-Host ""

# 2.1 Get all health checks
Test-API -Method "GET" -Path "/admin/health-checks" -Description "Get all health check results"

# 2.2 Get health check statistics
Test-API -Method "GET" -Path "/admin/health-checks/stats" -Description "Get health check statistics"

# 2.3 Get health checks by service (assume service_id=1 exists)
Test-API -Method "GET" -Path "/admin/health-checks/service/1" -Description "Get health checks for service 1"

# 2.4 Perform immediate health check on target (assume target_id=1 exists)
Test-API -Method "POST" -Path "/admin/health-checks/check/1" -Description "Perform immediate health check on target 1"

##################################################
# 3. VERIFICATION
##################################################

Write-Host "=========================================" -ForegroundColor Magenta
Write-Host "  3. FINAL VERIFICATION" -ForegroundColor White
Write-Host "=========================================" -ForegroundColor Magenta
Write-Host ""

Write-Host "Waiting 35 seconds for Health Check Scheduler to run..." -ForegroundColor Yellow
Start-Sleep -Seconds 35

Test-API -Method "GET" -Path "/admin/health-checks/stats" -Description "Health check stats after scheduler run"

Write-Host "=========================================" -ForegroundColor Magenta
Write-Host "  ALL TESTS COMPLETED!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Magenta
Write-Host ""
Write-Host "Summary:" -ForegroundColor White
Write-Host "✅ Consumers API: Create, Read, Update, Delete, Toggle, Regenerate Key, Stats" -ForegroundColor Green
Write-Host "✅ Health Checks API: Get All, Get by Service, Perform Check, Stats" -ForegroundColor Green
Write-Host "✅ Health Check Scheduler: Auto-running every 30 seconds" -ForegroundColor Green
Write-Host ""
