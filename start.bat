@echo off
setlocal EnableExtensions

set "ROOT=%~dp0"
set "BACKEND_DIR=%ROOT%online-shop-backend"
set "FRONTEND_DIR=%ROOT%online-shop-frontend"
set "BACKEND_PORT=8080"
set "FRONTEND_PORT=5173"
set "BACKEND_URL=http://localhost:%BACKEND_PORT%/api/products"
set "FRONTEND_URL=http://localhost:%FRONTEND_PORT%"

echo ========================================
echo Online Shop Platform Startup
echo Root: %ROOT%
echo Database profile: sqlite
echo ========================================

call :kill_window "online-shop-backend"
call :kill_window "online-shop-frontend"
call :kill_port %BACKEND_PORT%
call :kill_port %FRONTEND_PORT%

echo.
echo Starting backend on port %BACKEND_PORT%...
start "online-shop-backend" /D "%BACKEND_DIR%" cmd /k mvn spring-boot:run -Dspring-boot.run.profiles=sqlite

echo Starting frontend on port %FRONTEND_PORT%...
start "online-shop-frontend" /D "%FRONTEND_DIR%" cmd /k npm run dev -- --host 0.0.0.0 --port %FRONTEND_PORT%

echo.
echo Startup commands have been sent.
echo Backend API: %BACKEND_URL%
echo Frontend:    %FRONTEND_URL%
echo.
echo Waiting for services to bind ports...
call :wait_port %BACKEND_PORT% "backend"
call :wait_port %FRONTEND_PORT% "frontend"

call :show_port %BACKEND_PORT%
call :show_port %FRONTEND_PORT%

echo.
echo Opening browser...
start "" "%BACKEND_URL%"
start "" "%FRONTEND_URL%"

echo.
echo Done.
exit /b 0

:kill_window
set "TITLE=%~1"
echo.
echo Closing old window %TITLE% if it exists...
taskkill /F /T /FI "WINDOWTITLE eq %TITLE%" > nul 2>&1
taskkill /F /T /FI "WINDOWTITLE eq %TITLE%*" > nul 2>&1
exit /b 0

:kill_port
set "PORT=%~1"
echo.
echo Checking port %PORT%...
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":%PORT% .*LISTENING"') do (
    echo Killing process %%P on port %PORT%...
    taskkill /F /T /PID %%P > nul 2>&1
)
exit /b 0

:show_port
set "PORT=%~1"
set "FOUND="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":%PORT% .*LISTENING"') do (
    set "FOUND=1"
    echo Port %PORT% is listening, PID %%P.
)
if not defined FOUND echo Port %PORT% is not listening yet. Check logs if this continues.
exit /b 0

:wait_port
set "PORT=%~1"
set "NAME=%~2"
set "ATTEMPTS=0"
:wait_port_loop
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":%PORT% .*LISTENING"') do (
    echo %NAME% port %PORT% is ready, PID %%P.
    exit /b 0
)
set /a ATTEMPTS+=1
if %ATTEMPTS% GEQ 30 (
    echo %NAME% port %PORT% is not ready after 30 seconds.
    exit /b 0
)
ping 127.0.0.1 -n 2 > nul
goto wait_port_loop
