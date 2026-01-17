# eraf-commons-aggregator 빌드 테스트 스크립트
Set-Location $PSScriptRoot

$results = @()

function Test-Build {
    param(
        [string]$TestName,
        [string]$Profile
    )

    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host "Test: $TestName" -ForegroundColor Cyan
    Write-Host "Profile: $Profile" -ForegroundColor Yellow
    Write-Host "============================================" -ForegroundColor Cyan

    $command = "mvn clean package -P $Profile -DskipTests"
    Write-Host "Running: $command" -ForegroundColor Gray

    $output = & cmd /c $command 2>&1

    if ($LASTEXITCODE -eq 0) {
        $jarFile = Get-Item "target\eraf-commons-1.0.0-SNAPSHOT.jar" -ErrorAction SilentlyContinue
        if ($jarFile) {
            $sizeKB = [math]::Round($jarFile.Length / 1KB, 2)
            Write-Host "[SUCCESS] JAR Size: $sizeKB KB" -ForegroundColor Green

            # JAR 내용 확인
            $jarContents = & jar -tf $jarFile.FullName | Where-Object { $_ -match "^com/eraf/starter/(\w+)/" } | ForEach-Object {
                if ($_ -match "com/eraf/starter/(\w+)/") {
                    $matches[1]
                }
            } | Select-Object -Unique | Sort-Object

            $starters = $jarContents -join ", "
            Write-Host "Starters included: $starters" -ForegroundColor Gray

            $script:results += [PSCustomObject]@{
                Test = $TestName
                Profile = $Profile
                Status = "SUCCESS"
                Size_KB = $sizeKB
                Starters = $starters
            }
        } else {
            Write-Host "[FAILED] JAR file not found" -ForegroundColor Red
            $script:results += [PSCustomObject]@{
                Test = $TestName
                Profile = $Profile
                Status = "FAILED"
                Size_KB = 0
                Starters = "N/A"
            }
        }
    } else {
        Write-Host "[FAILED] Build failed" -ForegroundColor Red
        Write-Host $output -ForegroundColor Red
        $script:results += [PSCustomObject]@{
            Test = $TestName
            Profile = $Profile
            Status = "FAILED"
            Size_KB = 0
            Starters = "Build Error"
        }
    }

    Write-Host ""
}

# 테스트 실행
Test-Build "FTP only" "ftp"
Test-Build "Redis + Kafka" "redis,kafka"
Test-Build "Web + JPA + Database" "web,jpa,database"
Test-Build "Minimal (core only)" "minimal"
Test-Build "Redis only" "redis"
Test-Build "Full (all starters)" "full"

# 결과 요약
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "TEST RESULTS SUMMARY" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
$results | Format-Table -AutoSize
Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
