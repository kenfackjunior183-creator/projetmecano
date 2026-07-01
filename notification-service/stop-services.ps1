# Script d'arrêt des services Mecano pour Windows PowerShell
# Usage: .\stop-services.ps1

Write-Host "=====================================" -ForegroundColor Red
Write-Host "  Arrêt des services Mecano" -ForegroundColor Red
Write-Host "=====================================" -ForegroundColor Red
Write-Host ""

# Arrêter les conteneurs Docker
Write-Host "[1/2] Arrêt des conteneurs Docker..." -ForegroundColor Yellow

$postgresRunning = docker ps --filter "name=postgres" --filter "status=running" -q
$rabbitmqRunning = docker ps --filter "name=rabbitmq" --filter "status=running" -q

if ($postgresRunning) {
    docker stop postgres
    Write-Host "  ✓ PostgreSQL arrêté" -ForegroundColor Green
} else {
    Write-Host "  - PostgreSQL n'était pas en cours" -ForegroundColor Gray
}

if ($rabbitmqRunning) {
    docker stop rabbitmq
    Write-Host "  ✓ RabbitMQ arrêté" -ForegroundColor Green
} else {
    Write-Host "  - RabbitMQ n'était pas en cours" -ForegroundColor Gray
}

# Arrêter les processus Java
Write-Host "[2/2] Arrêt des services Spring Boot..." -ForegroundColor Yellow

$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    $javaProcesses | Stop-Process -Force
    Write-Host "  ✓ Services Spring Boot arrêtés" -ForegroundColor Green
} else {
    Write-Host "  - Aucun service Java en cours" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Green
Write-Host "  Services arrêtés !" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""

# Optionnel: Supprimer les conteneurs
Write-Host "💡 Pour supprimer les conteneurs (optionnel) :" -ForegroundColor Yellow
Write-Host "   docker rm postgres rabbitmq" -ForegroundColor Gray
Write-Host ""