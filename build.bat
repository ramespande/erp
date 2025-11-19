@echo off
setlocal enabledelayedexpansion

set MAIN_CLASS=%MAIN_CLASS: =%
if "%MAIN_CLASS%"=="" set MAIN_CLASS=edu.univ.erp.Application

if exist out\classes rmdir /s /q out\classes
mkdir out\classes

set CLASSPATH=out\classes
for %%f in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%f

if exist sources-main.txt del sources-main.txt
for /r src\main\java %%f in (*.java) do echo %%f>>sources-main.txt
if not exist sources-main.txt (
    for /r src %%f in (*.java) do echo %%f>>sources-main.txt
)

if not exist sources-main.txt (
    echo No Java sources found.
    exit /b 1
)

javac -d out\classes -classpath "%CLASSPATH%" @sources-main.txt
del sources-main.txt

set RESOURCE_DIR=src\main\resources
if not exist %RESOURCE_DIR% set RESOURCE_DIR=src\resources
if exist %RESOURCE_DIR% (
    xcopy /e /y /q %RESOURCE_DIR% out\classes >nul
)

echo Compiled application classes into out\classes
echo To run: set MAIN_CLASS=%MAIN_CLASS% ^&^& run.bat

