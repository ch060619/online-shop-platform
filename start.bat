@echo off
setlocal EnableExtensions

set "ROOT=%~dp0"
set "BACKEND_DIR=%ROOT%online-shop-backend"
set "FRONTEND_DIR=%ROOT%online-shop-frontend"
set "MODE=%~1"
if "%MODE%"=="" set "MODE=local"
set "BACKEND_PORT=8080"
set "FRONTEND_PORT=5173"
set "BACKEND_URL=http://localhost:%BACKEND_PORT%/api/products"
set "FRONTEND_URL=http://localhost:%FRONTEND_PORT%"

echo ========================================
echo Online Shop Platform V2.0 Startup
echo Root: %ROOT%
echo Mode: %MODE%
echo ========================================

call :kill_window "online-shop-backend"
call :kill_window "online-shop-frontend"
call :kill_port %BACKEND_PORT%
call :kill_port %FRONTEND_PORT%

if /I "%MODE%"=="docker" goto docker_mode
if /I not "%MODE%"=="local" (
    echo Unknown mode "%MODE%". Use "local" or "docker".
    exit /b 1
)

echo.
echo Preparing V2.0 infrastructure dependencies...
call :start_infra
call :wait_port 6379 "redis"
call :wait_port 5672 "rabbitmq"

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

:docker_mode
echo.
echo Starting Docker Compose services: MySQL, Redis, RabbitMQ, backend...
call :ensure_docker
if errorlevel 1 (
    echo Docker is not ready. Cannot start docker mode.
    exit /b 1
)
docker compose up --build -d
if errorlevel 1 (
    echo Docker Compose startup failed.
    exit /b 1
)

echo Starting frontend on port %FRONTEND_PORT%...
start "online-shop-frontend" /D "%FRONTEND_DIR%" cmd /k npm run dev -- --host 0.0.0.0 --port %FRONTEND_PORT%

echo.
echo Startup commands have been sent.
echo Backend API: %BACKEND_URL%
echo Frontend:    %FRONTEND_URL%
echo RabbitMQ UI: http://localhost:15672
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
exit /b 0

:start_infra
where docker > nul 2>&1
if errorlevel 1 (
    echo Docker was not found. Redis and RabbitMQ must be running on localhost for all V2.0 flows.
    exit /b 0
)
call :ensure_docker
if errorlevel 1 (
    echo Docker is not ready. Redis and RabbitMQ must be started manually.
    exit /b 0
)
docker compose up -d redis rabbitmq
if errorlevel 1 (
    echo Could not start Redis/RabbitMQ with Docker. Continuing with local app startup.
)
exit /b 0

:ensure_docker
docker info > nul 2>&1
if not errorlevel 1 (
    echo Docker daemon is ready.
    exit /b 0
)

echo Docker daemon is not running. Trying to start Docker Desktop...
call :start_docker_desktop
if errorlevel 1 exit /b 1

set "DOCKER_ATTEMPTS=0"
:wait_docker_loop
docker info > nul 2>&1
if not errorlevel 1 (
    echo Docker daemon is ready.
    exit /b 0
)
set /a DOCKER_ATTEMPTS+=1
if %DOCKER_ATTEMPTS% GEQ 90 (
    echo Docker daemon is not ready after 90 seconds.
    exit /b 1
)
ping 127.0.0.1 -n 2 > nul
goto wait_docker_loop

:start_docker_desktop
set "DOCKER_DESKTOP="
if exist "%ProgramFiles%\Docker\Docker\Docker Desktop.exe" set "DOCKER_DESKTOP=%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
if not defined DOCKER_DESKTOP if exist "%LOCALAPPDATA%\Docker\Docker Desktop.exe" set "DOCKER_DESKTOP=%LOCALAPPDATA%\Docker\Docker Desktop.exe"
if not defined DOCKER_DESKTOP if exist "%ProgramFiles(x86)%\Docker\Docker\Docker Desktop.exe" set "DOCKER_DESKTOP=%ProgramFiles(x86)%\Docker\Docker\Docker Desktop.exe"

if not defined DOCKER_DESKTOP (
    echo Docker Desktop executable was not found.
    exit /b 1
)

echo Starting Docker Desktop: %DOCKER_DESKTOP%
start "" "%DOCKER_DESKTOP%"
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
