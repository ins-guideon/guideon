@echo off
REM Guideon Regulation Search - All Tests Runner

echo ================================================
echo Guideon Regulation Search - Test Suite
echo ================================================
echo.

REM Check if API key is configured
if "%GOOGLE_API_KEY%"=="" (
    echo [WARNING] GOOGLE_API_KEY environment variable is not set.
    echo Some tests will be skipped.
    echo.
    echo To run all tests, set your API key:
    echo   set GOOGLE_API_KEY=your_api_key_here
    echo.
    pause
)

echo Running all tests...
echo.

mvn test

echo.
echo ================================================
echo Test execution completed!
echo ================================================
pause
