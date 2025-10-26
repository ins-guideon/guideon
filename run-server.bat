@echo off
REM Guideon REST API Server - Run Script

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

echo Starting Spring Boot server on port 8080...
echo.

mvn spring-boot:run

pause
