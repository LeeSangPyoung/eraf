@echo off
REM eraf-commons full 빌드 및 eraf-api-gateway 통합 테스트

echo ============================================
echo Step 1: Build eraf-commons-aggregator (FULL)
echo ============================================
cd /d "%~dp0\eraf-commons\eraf-commons-aggregator"
call mvn clean install -P full -DskipTests
if errorlevel 1 (
    echo [FAILED] eraf-commons-aggregator build failed
    pause
    exit /b 1
)
echo.
echo [SUCCESS] eraf-commons-aggregator installed to local Maven repo
dir target\eraf-commons-1.0.0-SNAPSHOT.jar | find "eraf-commons"
echo.

echo ============================================
echo Step 2: Build eraf-api-gateway (FULL)
echo ============================================
cd /d "%~dp0\eraf-api-gateway\eraf-gateway-builder"
call mvn clean package -P full -DskipTests
if errorlevel 1 (
    echo [FAILED] eraf-api-gateway build failed
    pause
    exit /b 1
)
echo.
echo [SUCCESS] eraf-api-gateway built
dir target\eraf-gateway-1.0.0-SNAPSHOT.jar | find "eraf-gateway"
echo.

echo ============================================
echo Step 3: Run eraf-api-gateway
echo ============================================
echo Starting gateway... (Press Ctrl+C to stop)
echo.
java -jar target\eraf-gateway-1.0.0-SNAPSHOT.jar
