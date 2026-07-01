@echo off
REM Script de démarrage des services Mecano pour Windows (Batch)
REM Usage: start-services.bat

echo =====================================
echo   Demarrage des services Mecano
echo =====================================
echo.

REM 1. Discovery Server (Eureka)
echo [1/6] Demarrage de Discovery Server (Eureka)...
start "Discovery Server" cmd /c "cd /d discovery-server && mvnw.cmd spring-boot:run"
echo   ✓ Discovery Server demarre (http://localhost:8761)
timeout /t 5 /nobreak > nul

REM 2. Config Server
echo [2/6] Demarrage de Config Server...
start "Config Server" cmd /c "cd /d config-server && mvnw.cmd spring-boot:run"
echo   ✓ Config Server demarre (http://localhost:8888)
timeout /t 5 /nobreak > nul

REM 3. API Gateway
echo [3/6] Demarrage de API Gateway...
start "API Gateway" cmd /c "cd /d api-gateway && mvnw.cmd spring-boot:run"
echo   ✓ API Gateway demarre (http://localhost:8080)
timeout /t 5 /nobreak > nul

REM 4. PostgreSQL (via Docker)
echo [4/6] Verification de PostgreSQL...
docker ps --filter "name=postgres" --filter "status=running" | findstr postgres >nul
if errorlevel 1 (
    echo   Demarrage de PostgreSQL...
    docker run -d --name postgres -p 5432:5432 -e POSTGRES_DB=mecano_notif_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=0116 postgres:15
    echo   ✓ PostgreSQL demarre (localhost:5432)
) else (
    echo   ✓ PostgreSQL deja en cours d'execution
)

REM 5. RabbitMQ (via Docker)
echo [5/6] Verification de RabbitMQ...
docker ps --filter "name=rabbitmq" --filter "status=running" | findstr rabbitmq >nul
if errorlevel 1 (
    echo   Demarrage de RabbitMQ...
    docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
    echo   ✓ RabbitMQ demarre (http://localhost:15672)
) else (
    echo   ✓ RabbitMQ deja en cours d'execution
)

timeout /t 3 /nobreak > nul

REM 6. Notification Service
echo [6/6] Demarrage de Notification Service...
start "Notification Service" cmd /c "cd /d notification-service && mvnw.cmd spring-boot:run"
echo   ✓ Notification Service demarre (http://localhost:8085)

echo.
echo =====================================
echo   Services demarres !
echo =====================================
echo.
echo URLs importantes :
echo   • Eureka Dashboard : http://localhost:8761
echo   • Config Server     : http://localhost:8888
echo   • API Gateway       : http://localhost:8080
echo   • RabbitMQ UI       : http://localhost:15672 (guest/guest)
echo   • Notification API  : http://localhost:8085/api/notifications/health
echo.
echo Pour arreter les services, fermez les fenetres ou utilisez : stop-services.bat
echo.
pause