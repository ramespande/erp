@echo off
setlocal enabledelayedexpansion

REM Load database configuration if config.bat exists
if exist config.bat (
    call config.bat
) else (
    echo Warning: config.bat not found. Using default database settings.
    echo Create config.bat to customize database connection.
    echo.
)

if "%MAIN_CLASS%"=="" set MAIN_CLASS=edu.univ.erp.Application

if not exist out\classes (
    echo Classes not found. Run build.bat first.
    exit /b 1
)

set CLASSPATH=out\classes
for %%f in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%f

java -cp "%CLASSPATH%" %MAIN_CLASS% %*