@echo off
REM Script d'arrêt des services Mecano pour Windows (Batch)
REM Usage: stop-services.bat

echo =====================================
echo   Arret des services Mecano
echo =====================================
echo.

REM Arrêter les conteneurs Docker
echo [1/2] Arret des conteneurs Docker...

docker ps --filter "name=postgres" --filter "status=running" | findstr postgres >nul
if not errorlevel 1 (
    docker stop postgres
    echo   ✓ PostgreSQL arrete
) else (
    echo   - PostgreSQL n'etait pas en cours
)

docker ps --filter "name=rabbitmq" --filter "status=running" | findstr rabbitmq >nul
if not errorlevel 1 (
    docker stop rabbitmq
    echo   ✓ RabbitMQ arrete
) else (
    echo   - RabbitMQ n'etait pas en cours
)

REM Arrêter les processus Java
echo [2/2] Arret des services Spring Boot...

tasklist /FI "IMAGENAME eq java.exe" | findstr /I "java.exe" >nul
if not errorlevel 1 (
    taskkill /F /IM java.exe
    echo   ✓ Services Spring Boot arretes
) else (
    echo   - Aucun service Java en cours
)

echo.
echo =====================================
echo   Services arretes !
echo =====================================
echo.
echo Pour supprimer les conteneurs (optionnel) :
echo   docker rm postgres rabbitmq
echo.
pause