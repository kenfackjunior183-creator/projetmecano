# Script de démarrage des services Mecano pour Windows PowerShell
# Usage: .\start-services.ps1

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Démarrage des services Mecano" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Fonction pour vérifier si un service est déjà en cours d'exécution
function Test-ServiceRunning {
    param($ServiceName)
    $process = Get-Process -Name $ServiceName -ErrorAction SilentlyContinue
    return $null -ne $process
}

# 1. Discovery Server (Eureka)
Write-Host "[1/6] Démarrage de Discovery Server (Eureka)..." -ForegroundColor Yellow
Set-Location discovery-server
if (-not (Test-ServiceRunning "java")) {
    Start-Process powershell -ArgumentList "-NoExit", "./mvnw.cmd spring-boot:run" -WindowStyle Normal
    Write-Host "  ✓ Discovery Server démarré (http://localhost:8761)" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Discovery Server semble déjà en cours" -ForegroundColor Orange
}
Set-Location ..

# Attendre 5 secondes pour que Eureka démarre
Write-Host "  ⏳ Attente de 5 secondes..." -ForegroundColor Gray
Start-Sleep -Seconds 5

# 2. Config Server
Write-Host "[2/6] Démarrage de Config Server..." -ForegroundColor Yellow
Set-Location config-server
if (-not (Test-ServiceRunning "java")) {
    Start-Process powershell -ArgumentList "-NoExit", "./mvnw.cmd spring-boot:run" -WindowStyle Normal
    Write-Host "  ✓ Config Server démarré (http://localhost:8888)" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Config Server semble déjà en cours" -ForegroundColor Orange
}
Set-Location ..

Start-Sleep -Seconds 5

# 3. API Gateway
Write-Host "[3/6] Démarrage de API Gateway..." -ForegroundColor Yellow
Set-Location api-gateway
if (-not (Test-ServiceRunning "java")) {
    Start-Process powershell -ArgumentList "-NoExit", "./mvnw.cmd spring-boot:run" -WindowStyle Normal
    Write-Host "  ✓ API Gateway démarré (http://localhost:8080)" -ForegroundColor Green
} else {
    Write-Host "  ⚠ API Gateway semble déjà en cours" -ForegroundColor Orange
}
Set-Location ..

Start-Sleep -Seconds 5

# 4. PostgreSQL (via Docker)
Write-Host "[4/6] Vérification de PostgreSQL..." -ForegroundColor Yellow
$postgresRunning = docker ps --filter "name=postgres" --filter "status=running" -q
if (-not $postgresRunning) {
    Write-Host "  Démarrage de PostgreSQL..." -ForegroundColor Gray
    docker run -d --name postgres -p 5432:5432 `
        -e POSTGRES_DB=mecano_notif_db `
        -e POSTGRES_USER=postgres `
        -e POSTGRES_PASSWORD=0116 `
        postgres:15
    Write-Host "  ✓ PostgreSQL démarré (localhost:5432)" -ForegroundColor Green
} else {
    Write-Host "  ✓ PostgreSQL déjà en cours d'exécution" -ForegroundColor Green
}

# 5. RabbitMQ (via Docker)
Write-Host "[5/6] Vérification de RabbitMQ..." -ForegroundColor Yellow
$rabbitmqRunning = docker ps --filter "name=rabbitmq" --filter "status=running" -q
if (-not $rabbitmqRunning) {
    Write-Host "  Démarrage de RabbitMQ..." -ForegroundColor Gray
    docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
    Write-Host "  ✓ RabbitMQ démarré (http://localhost:15672)" -ForegroundColor Green
} else {
    Write-Host "  ✓ RabbitMQ déjà en cours d'exécution" -ForegroundColor Green
}

Start-Sleep -Seconds 3

# 6. Notification Service
Write-Host "[6/6] Démarrage de Notification Service..." -ForegroundColor Yellow
Set-Location notification-service
if (-not (Test-ServiceRunning "java")) {
    Start-Process powershell -ArgumentList "-NoExit", "./mvnw.cmd spring-boot:run" -WindowStyle Normal
    Write-Host "  ✓ Notification Service démarré (http://localhost:8085)" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Notification Service semble déjà en cours" -ForegroundColor Orange
}
Set-Location ..

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Services démarrés !" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 URLs importantes :" -ForegroundColor White
Write-Host "  • Eureka Dashboard : http://localhost:8761" -ForegroundColor Cyan
Write-Host "  • Config Server     : http://localhost:8888" -ForegroundColor Cyan
Write-Host "  • API Gateway       : http://localhost:8080" -ForegroundColor Cyan
Write-Host "  • RabbitMQ UI       : http://localhost:15672 (guest/guest)" -ForegroundColor Cyan
Write-Host "  • Notification API  : http://localhost:8085/api/notifications/health" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Pour arrêter les services, fermez les fenêtres PowerShell ou utilisez :" -ForegroundColor Yellow
Write-Host "   docker stop postgres rabbitmq" -ForegroundColor Gray
Write-Host ""