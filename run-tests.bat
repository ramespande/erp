@echo off
setlocal enabledelayedexpansion

if "%JUNIT_JAR%"=="" (
    for %%f in (lib\junit-platform-console-standalone-*.jar) do set JUNIT_JAR=%%f
)

if "%JUNIT_JAR%"=="" (
    echo JUnit console jar not found. Place it in lib\ or set JUNIT_JAR.
    exit /b 1
)

call build.bat

if exist out\test-classes rmdir /s /q out\test-classes
mkdir out\test-classes

set CLASSPATH=out\classes
for %%f in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%f
set CLASSPATH=%CLASSPATH%;%JUNIT_JAR%

if exist sources-test.txt del sources-test.txt
for /r src\test\java %%f in (*.java) do echo %%f>>sources-test.txt

javac -d out\test-classes -classpath "%CLASSPATH%" @sources-test.txt
del sources-test.txt

set TEST_CP=out\test-classes;out\classes
for %%f in (lib\*.jar) do set TEST_CP=!TEST_CP!;%%f

java -jar "%JUNIT_JAR%" --class-path "%TEST_CP%" --scan-classpath

