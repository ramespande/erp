@echo off
setlocal enabledelayedexpansion

if "%MAIN_CLASS%"=="" set MAIN_CLASS=edu.univ.erp.Application

call build.bat

set DIST_DIR=dist\erp-app
if exist %DIST_DIR% rmdir /s /q %DIST_DIR%
mkdir %DIST_DIR%
mkdir %DIST_DIR%\lib

xcopy /E /I /Q /Y out\classes %DIST_DIR%\classes >nul
for %%f in (lib\*.jar) do copy /Y %%f %DIST_DIR%\lib\ >nul

(
echo @echo off
echo setlocal enabledelayedexpansion
echo set BASE_DIR=%%~dp0
echo set CLASSPATH=%%BASE_DIR%%classes
echo for %%%%f in (%%BASE_DIR%%lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%%%f
echo if "%%MAIN_CLASS%%"=="" set MAIN_CLASS=edu.univ.erp.Application
echo java -cp "%%CLASSPATH%%" %%MAIN_CLASS%% %%*
) > %DIST_DIR%\run.bat

jar --create --file dist\erp-app.jar --main-class %MAIN_CLASS% -C out\classes .

echo Distribution created under dist\erp-app and dist\erp-app.jar

