@echo off
echo ===== Executing SQL scripts =====

echo.
echo [1/2] Creating gateway_consumers table and sample data...
psql -U eraf -d eraf_gateway -f consumers-init.sql

echo.
echo [2/2] Registering Admin APIs (Consumers + Health Checks)...
psql -U eraf -d eraf_gateway -f admin-apis-init.sql

echo.
echo ===== SQL scripts completed =====
