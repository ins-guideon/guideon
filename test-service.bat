@echo off
REM Guideon Regulation Search - Service Tests Runner

echo ================================================
echo Service-specific Test Runner
echo ================================================
echo.

if "%1"=="" (
    echo Usage: test-service.bat [service-name]
    echo.
    echo Available services:
    echo   1. QueryAnalysisService
    echo   2. RegulationSearchService
    echo   3. RegulationQASystem
    echo   4. all - Run all tests
    echo.
    echo Example: test-service.bat QueryAnalysisService
    echo.
    pause
    exit /b 1
)

if "%1"=="all" (
    echo Running all tests...
    mvn test
    goto end
)

echo Running tests for: %1
echo.

mvn test -Dtest=%1Test

:end
echo.
echo ================================================
echo Test execution completed!
echo ================================================
pause
