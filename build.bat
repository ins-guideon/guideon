@echo off
REM Guideon - Build Script

echo ================================================
echo Guideon - Maven Build
echo ================================================
echo.

REM Set JAVA_HOME to Java 17
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using Java version:
java -version
echo.

echo Building project...
echo.

mvn clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================================
    echo Build SUCCESS!
    echo ================================================
    echo JAR file created: target\regulation-search-1.0.0.jar
    echo.
    echo To run the server:
    echo   java -jar target\regulation-search-1.0.0.jar
    echo   OR
    echo   run-server.bat
) else (
    echo.
    echo ================================================
    echo Build FAILED!
    echo ================================================
)

pause
