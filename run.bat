@echo off
setlocal enabledelayedexpansion

if "%MAIN_CLASS%"=="" set MAIN_CLASS=edu.univ.erp.Application

if not exist out\classes (
    echo Classes not found. Run build.bat first.
    exit /b 1
)

set CLASSPATH=out\classes
for %%f in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%f

java -cp "%CLASSPATH%" %MAIN_CLASS% %*

