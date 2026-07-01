# 📜 Scripts de gestion des services Mecano

## 🚀 Démarrage rapide

### ⭐ Option 1 : Script Batch (RECOMMANDÉ - compatible Windows)

```cmd
# Ouvrir CMD ou PowerShell
# Se placer dans le dossier du projet
cd C:\Users\Borel\Desktop\projetmecano\notification-service

# Démarrer tous les services
start-services.bat
```

**Avantages** :
- ✅ Pas de problème de politique d'exécution
- ✅ Compatible CMD et PowerShell
- ✅ Fonctionne immédiatement

**Ce que fait le script** :
1. ✅ Démarre Discovery Server (Eureka) sur le port 8761
2. ✅ Démarre Config Server sur le port 8888
3. ✅ Démarre API Gateway sur le port 8080
4. ✅ Démarre PostgreSQL sur le port 5432 (via Docker)
5. ✅ Démarre RabbitMQ sur les ports 5672/15672 (via Docker)
6. ✅ Démarre Notification Service sur le port 8085

Chaque service s'ouvre dans une fenêtre CMD séparée.

---

### Option 2 : Script PowerShell (si vous préférez PowerShell)

```powershell
# Ouvrir PowerShell en tant qu'administrateur
cd C:\Users\Borel\Desktop\projetmecano\notification-service

# Si vous avez une erreur de politique d'exécution, utilisez :
Set-ExecutionPolicy Bypass -Scope Process -Force
.\start-services.ps1
```

**Note** : Si vous obtenez l'erreur *"Impossible de charger le fichier... l'exécution de scripts est désactivée"*, utilisez plutôt le script Batch (Option 1).

### Option 3 : Démarrage manuel (si vous préférez)

```powershell
# Terminal 1 - Discovery Server
Set-Location discovery-server
.\mvnw.cmd spring-boot:run

# Terminal 2 - Config Server
Set-Location config-server
.\mvnw.cmd spring-boot:run

# Terminal 3 - API Gateway
Set-Location api-gateway
.\mvnw.cmd spring-boot:run

# Terminal 4 - Notification Service
Set-Location notification-service
.\mvnw.cmd spring-boot:run
```

**Puis dans des terminaux séparés** :
```powershell
# Terminal 5 - PostgreSQL (Docker)
docker run -d --name postgres -p 5432:5432 -e POSTGRES_DB=mecano_notif_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=sage237@ postgres:15

# Terminal 6 - RabbitMQ (Docker)
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
```

---

## 🛑 Arrêter les services

### ⭐ Option 1 : Script Batch d'arrêt (RECOMMANDÉ)

```cmd
# Depuis le dossier notification-service
stop-services.bat
```

### Option 2 : Script PowerShell d'arrêt

```powershell
# Depuis le dossier notification-service
.\stop-services.ps1
```

### Option 3 : Arrêt manuel

```powershell
# Arrêter les conteneurs Docker
docker stop postgres rabbitmq

# Arrêter les services Spring Boot
# Fermer les fenêtres PowerShell ou :
Get-Process -Name "java" | Stop-Process -Force
```

---

## 🔧 Résolution des problèmes courants

### ❌ Problème 1 : `&&` n'est pas un séparateur valide

**Erreur** :
```powershell
cd discovery-server && ./mvnw spring-boot:run
# Le jeton «&&» n'est pas un séparateur d'instruction valide.
```

**Solution** : Utiliser le script Batch fourni
```cmd
start-services.bat
```

Ou utiliser des points-virgules dans PowerShell :
```powershell
Set-Location discovery-server; .\mvnw.cmd spring-boot:run
```

---

### ❌ Problème 2 : "L'exécution de scripts est désactivée"

**Erreur** :
```
Impossible de charger le fichier ... car l'exécution de scripts est désactivée sur ce système.
```

**Solution 1** : Utiliser le script Batch (recommandé)
```cmd
start-services.bat
```

**Solution 2** : Modifier la politique d'exécution PowerShell (temporaire)
```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force
.\start-services.ps1
```

**Solution 3** : Modifier la politique d'exécution (permanent - nécessite admin)
```powershell
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

---

## 📋 Vérification des services

### Vérifier que tous les services sont UP

```powershell
# 1. Eureka Dashboard
Start-Process "http://localhost:8761"

# 2. Config Server
curl http://localhost:8888/notification-service/default

# 3. API Gateway
curl http://localhost:8080/actuator/health

# 4. Notification Service
curl http://localhost:8085/api/notifications/health
# Réponse attendue : "Notification Service UP ✅"

# 5. RabbitMQ Management UI
Start-Process "http://localhost:15672"
# Login: guest / Password: guest

# 6. PostgreSQL
docker ps | findstr postgres
```

---

## 🐳 Gestion de Docker

### Voir les conteneurs en cours
```powershell
docker ps
```

### Voir tous les conteneurs (même arrêtés)
```powershell
docker ps -a
```

### Arrêter un conteneur spécifique
```powershell
docker stop postgres
docker stop rabbitmq
```

### Supprimer un conteneur
```powershell
docker rm postgres
docker rm rabbitmq
```

### Voir les logs d'un conteneur
```powershell
docker logs postgres
docker logs rabbitmq
```

---

## 🔍 Dépannage

### Problème : Port déjà utilisé

**Symptôme** : `Port 8080 was already in use`

**Solution** :
```powershell
# Trouver le processus utilisant le port
netstat -ano | findstr :8080

# Tuer le processus (remplacer <PID> par l'ID trouvé)
Stop-Process -Id <PID> -Force
```

### Problème : Docker ne démarre pas

**Vérifier que Docker Desktop est lancé** :
```powershell
docker --version
docker ps
```

Si Docker n'est pas installé :
1. Télécharger Docker Desktop : https://www.docker.com/products/docker-desktop
2. Installer et redémarrer
3. Lancer Docker Desktop

### Problème : Service ne démarre pas

**Vérifier les logs** :
```powershell
# Les logs s'affichent dans la fenêtre PowerShell du service
# Ou vérifier les fichiers de log
Get-Content notification-service\logs\notification-service.log -Wait
```

### Problème : Base de données non accessible

**Vérifier PostgreSQL** :
```powershell
# Vérifier que le conteneur tourne
docker ps | findstr postgres

# Tester la connexion
docker exec -it postgres psql -U postgres -d mecano_notif_db
```

---

## 📊 Monitoring

### Voir les logs en temps réel

```powershell
# Logs du notification-service
Get-Content notification-service\logs\notification-service.log -Wait | Select-String "📨|✅|❌"

# Logs Docker PostgreSQL
docker logs -f postgres

# Logs Docker RabbitMQ
docker logs -f rabbitmq
```

### Vérifier Eureka

```powershell
# Dashboard
Start-Process "http://localhost:8761"

# Vérifier que notification-service est enregistré
curl http://localhost:8761/eureka/apps
```

### Vérifier RabbitMQ

```powershell
# UI Management
Start-Process "http://localhost:15672"

# Lister les queues
curl -u guest:guest http://localhost:15672/api/queues
```

---

## 🎯 Commandes rapides

### Avec les scripts Batch (recommandé)
```cmd
# Démarrer tous les services
start-services.bat

# Arrêter tous les services
stop-services.bat
```

### Avec PowerShell
```powershell
# Démarrer tous les services
.\start-services.ps1

# Arrêter tous les services
.\stop-services.ps1
```

### Commandes utiles
```powershell
# Tester le health check
curl http://localhost:8085/api/notifications/health

# Voir les logs du notification-service
Get-Content notification-service\logs\notification-service.log -Wait

# Redémarrer uniquement le notification-service
# 1. Fermer la fenêtre du notification-service
# 2. Dans un nouveau terminal :
Set-Location notification-service
.\mvnw.cmd spring-boot:run

# Vérifier les conteneurs Docker
docker ps

# Arrêter tous les conteneurs
docker stop $(docker ps -q)
```

---

## 📝 Notes importantes

1. **Scripts Batch** : Utilisez `start-services.bat` et `stop-services.bat` pour éviter les problèmes de politique d'exécution PowerShell
2. **Java 21** : Vérifier que Java 21 est installé : `java -version`
3. **Maven** : Les scripts utilisent `mvnw.cmd` (Maven Wrapper), pas besoin d'installer Maven
4. **Docker Desktop** : Doit être lancé avant d'exécuter les scripts
5. **Firewall** : Vérifier que le firewall Windows ne bloque pas les ports

---

## ✅ Checklist de démarrage

- [ ] Java 21 installé et configuré
- [ ] Docker Desktop lancé
- [ ] Ouvrir CMD ou PowerShell
- [ ] Exécuter `start-services.bat`
- [ ] Vérifier Eureka : http://localhost:8761
- [ ] Vérifier RabbitMQ : http://localhost:15672
- [ ] Vérifier Notification Service : http://localhost:8085/api/notifications/health
- [ ] Tester avec Postman (voir TESTING_GUIDE_POSTMAN.md)

---

## 🆘 Besoin d'aide ?

Si vous rencontrez des problèmes :

1. **Utiliser les scripts Batch** : `start-services.bat` et `stop-services.bat`
2. **Vérifier les logs** dans les fenêtres CMD/PowerShell
3. **Vérifier Docker** : `docker ps`
4. **Vérifier les ports** : `netstat -ano | findstr :8080`
5. **Redémarrer** : `stop-services.bat` puis `start-services.bat`

**Bon démarrage ! 🚀**