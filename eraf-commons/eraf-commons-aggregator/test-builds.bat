@echo off
REM eraf-commons-aggregator 빌드 테스트 스크립트
cd /d "%~dp0"

echo ============================================
echo Test 1: FTP only
echo ============================================
call mvn clean package -P ftp -DskipTests
if errorlevel 1 (
    echo [FAILED] FTP build failed
) else (
    dir target\eraf-commons-1.0.0-SNAPSHOT.jar | find "eraf-commons"
)
echo.

echo ============================================
echo Test 2: Redis + Kafka
echo ============================================
call mvn clean package -P redis,kafka -DskipTests
if errorlevel 1 (
    echo [FAILED] Redis+Kafka build failed
) else (
    dir target\eraf-commons-1.0.0-SNAPSHOT.jar | find "eraf-commons"
)
echo.

echo ============================================
echo Test 3: Web + JPA + Database
echo ============================================
call mvn clean package -P web,jpa,database -DskipTests
if errorlevel 1 (
    echo [FAILED] Web+JPA+Database build failed
) else (
    dir target\eraf-commons-1.0.0-SNAPSHOT.jar | find "eraf-commons"
)
echo.

echo ============================================
echo Test 4: Minimal (core only)
echo ============================================
call mvn clean package -P minimal -DskipTests
if errorlevel 1 (
    echo [FAILED] Minimal build failed
) else (
    dir target\eraf-commons-1.0.0-SNAPSHOT.jar | find "eraf-commons"
)
echo.

echo ============================================
echo Test 5: Redis only
echo ============================================
call mvn clean package -P redis -DskipTests
if errorlevel 1 (
    echo [FAILED] Redis build failed
) else (
    dir target\eraf-commons-1.0.0-SNAPSHOT.jar | find "eraf-commons"
)
echo.

echo ============================================
echo Test 6: Full (all starters)
echo ============================================
call mvn clean package -P full -DskipTests
if errorlevel 1 (
    echo [FAILED] Full build failed
) else (
    dir target\eraf-commons-1.0.0-SNAPSHOT.jar | find "eraf-commons"
)
echo.

echo ============================================
echo All tests completed!
echo ============================================
pause
