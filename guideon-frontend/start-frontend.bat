@echo off
REM Guideon Frontend - Run Script

echo ================================================
echo Guideon Frontend Server
echo ================================================
echo.

echo Checking Node.js installation...
node --version
echo.

echo Starting Vite dev server on port 3000...
echo.
echo Backend API should be running at http://localhost:8080
echo Frontend will be available at http://localhost:3000
echo.

npm run dev

pause
