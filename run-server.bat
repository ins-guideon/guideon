@echo off
REM Guideon REST API Server - Run Script

REM UTF-8 콘솔 인코딩 설정
chcp 65001 >nul

echo ================================================
echo Guideon REST API Server
echo ================================================
echo.

REM Set JAVA_HOME to Java 17
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using Java version:
java -version
echo.

REM Check if API key is set
if "%GOOGLE_API_KEY%"=="" (
    echo WARNING: GOOGLE_API_KEY environment variable is not set!
    echo Please set it using: set GOOGLE_API_KEY=your_api_key_here
    echo Or configure it in src/main/resources/application.yml
    echo.
)

REM Check if JAR file exists
if not exist "target\regulation-search-1.0.0.jar" (
    echo ERROR: JAR file not found: target\regulation-search-1.0.0.jar
    echo Please build the project first using: build.ps1 or mvn clean package
    echo.
    pause
    exit /b 1
)

echo Starting Spring Boot server on port 8080...
echo.

REM Run application with UTF-8 encoding settings
java "-Dfile.encoding=UTF-8" "-Dconsole.encoding=UTF-8" "-Djansi.passthrough=true" -jar target\regulation-search-1.0.0.jar

pause
