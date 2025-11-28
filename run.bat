@echo off
setlocal enabledelayedexpansion

REM ============================================================================
REM Database Configuration (inlined from config.bat)
REM ============================================================================
set "ERP_AUTH_DB_URL=jdbc:mysql://localhost:3306/erp_auth?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
set "ERP_AUTH_DB_USER=root"
set "ERP_AUTH_DB_PASSWORD=MCRajaZito1"
set "ERP_DATA_DB_URL=jdbc:mysql://localhost:3306/erp_data?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
set "ERP_DATA_DB_USER=root"
set "ERP_DATA_DB_PASSWORD=MCRajaZito1"
set "ERP_DB_POOL_SIZE=8"

echo Database configuration loaded.
echo Auth DB: "%ERP_AUTH_DB_URL%"
echo Data DB: "%ERP_DATA_DB_URL%"
echo.

if "%MAIN_CLASS%"=="" set MAIN_CLASS=edu.univ.erp.Application

if not exist out\classes (
    echo Classes not found. Run build.bat first.
    exit /b 1
)

set CLASSPATH=out\classes
for %%f in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%f

java -cp "%CLASSPATH%" %MAIN_CLASS% %*