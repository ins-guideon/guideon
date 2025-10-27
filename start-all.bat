@echo off
REM Guideon - Start Both Backend and Frontend

echo ================================================
echo Guideon Full Stack Application
echo ================================================
echo.

REM Set JAVA_HOME to Java 17
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

echo Starting Backend Server (Port 8080) and Frontend (Port 3000)...
echo.

REM Start Backend in a new window
echo [1/2] Starting Backend Server...
start "Guideon Backend (Port 8080)" cmd /k "cd /d %~dp0 && run-server.bat"

REM Wait a bit for backend to start
timeout /t 3 /nobreak >nul

REM Start Frontend in a new window
echo [2/2] Starting Frontend Server...
start "Guideon Frontend (Port 3000)" cmd /k "cd /d %~dp0guideon-frontend && npm run dev"

echo.
echo ================================================
echo Both servers are starting in separate windows!
echo ================================================
echo.
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:3000
echo.
echo Press any key to close this window (servers will keep running)...
pause >nul
