@echo off
REM Guideon Regulation Search System - Run Script

echo ================================================
echo Guideon Regulation Search System
echo ================================================
echo.

REM Check if custom config file is provided
if not "%1"=="" (
    echo Using custom configuration: %1
    "C:\Program Files\Java\jdk-17\bin\java" -jar target\regulation-search-1.0.0.jar %1
    goto end
)

echo Starting with default configuration...
echo.
echo Configuration methods (in priority order):
echo 1. Environment variable: GOOGLE_API_KEY
echo 2. application.properties file
echo 3. Custom config file: run.bat custom.properties
echo.

"C:\Program Files\Java\jdk-17\bin\java" -jar target\regulation-search-1.0.0.jar

:end
pause
