@echo off
REM ============================================================================
REM Database Configuration
REM Set your MySQL connection details here
REM ============================================================================

REM Auth Database Configuration
set "ERP_AUTH_DB_URL=jdbc:mysql://localhost:3306/erp_auth?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
set "ERP_AUTH_DB_USER=root"
set "ERP_AUTH_DB_PASSWORD=rijul"

REM ERP Database Configuration
set "ERP_DATA_DB_URL=jdbc:mysql://localhost:3306/erp_data?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
set "ERP_DATA_DB_USER=root"
set "ERP_DATA_DB_PASSWORD=rijul"

REM Connection Pool Size (optional)
set "ERP_DB_POOL_SIZE=8"

echo Database configuration loaded.
echo Auth DB: "%ERP_AUTH_DB_URL%"
echo Data DB: "%ERP_DATA_DB_URL%"
echo.