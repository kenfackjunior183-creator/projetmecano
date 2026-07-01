# 🐳 Résolution des problèmes Docker

## ❌ Erreur rencontrée

```
unable to get image 'projetmecano-notification-service': request returned 500 Internal Server Error for API route and version http://%2F%2F.%2Fpipe%2FdockerDesktopLinuxEngine/v1.54/images/projetmecano-notification-service/json, check if the server supports the requested API version
```

**Cause** : Docker Desktop a un problème avec son moteur Linux (WSL2).

---

## ✅ Solutions (essayer dans l'ordre)

### Solution 1 : Redémarrer Docker Desktop

1. **Fermer Docker Desktop**
   - Clic droit sur l'icône Docker dans la barre des tâches
   - Quitter

2. **Attendre 10 secondes**

3. **Relancer Docker Desktop**
   - Démarrer Docker Desktop depuis le menu Démarrer
   - Attendre que l'icône soit stable (verte)

4. **Vérifier que Docker fonctionne**
   ```cmd
   docker --version
   docker ps
   ```

5. **Relancer docker-compose**
   ```cmd
   cd C:\Users\Borel\Desktop\projetmecano
   docker-compose up --build
   ```

---

### Solution 2 : Redémarrer WSL2

Si Docker Desktop utilise WSL2 :

```cmd
# Ouvrir PowerShell en administrateur
wsl --shutdown

# Attendre 5 secondes
timeout /t 5

# Relancer Docker Desktop
# Puis vérifier
docker ps
```

---

### Solution 3 : Réinitialiser Docker Desktop

1. **Ouvrir Docker Desktop**
2. **Aller dans Settings (Paramètres)**
3. **Aller dans l'onglet "Resources" > "WSL Integration"**
4. **Désactiver puis réactiver l'intégration WSL2**
5. **Redémarrer Docker Desktop**

---

### Solution 4 : Nettoyer les ressources Docker

```cmd
# Arrêter tous les conteneurs
docker stop $(docker ps -q)

# Supprimer tous les conteneurs arrêtés
docker container prune -f

# Supprimer toutes les images
docker image prune -a -f

# Supprimer tous les volumes
docker volume prune -f

# Relancer docker-compose
cd C:\Users\Borel\Desktop\projetmecano
docker-compose up --build
```

---

### Solution 5 : Vérifier les prérequis

#### Vérifier que Docker Desktop est bien installé

```cmd
docker --version
# Doit afficher : Docker version 24.x.x ou plus récent

docker info
# Doit afficher des informations sur le daemon
```

#### Vérifier que WSL2 est installé (si applicable)

```cmd
wsl --version
# Doit afficher : WSL version 1.x.x ou plus récent
```

Si WSL2 n'est pas installé :
```cmd
# Installer WSL2
wsl --install

# Redémarrer Windows
shutdown /r /t 0
```

---

### Solution 6 : Utiliser les scripts Batch (ALTERNATIVE)

Si Docker continue de poser problème, utilisez les scripts Batch que j'ai créés :

```cmd
# Au lieu de docker-compose, utilisez :
cd C:\Users\Borel\Desktop\projetmecano\notification-service
start-services.bat
```

**Avantages** :
- ✅ Pas besoin de Docker pour les services Spring Boot
- ✅ Démarrage plus rapide
- ✅ Meilleur contrôle sur chaque service

---

## 🔍 Diagnostic détaillé

### Étape 1 : Vérifier l'état de Docker

```cmd
# Vérifier que le daemon Docker est en cours d'exécution
docker info

# Si erreur, Docker Desktop n'est pas démarré correctement
```

### Étape 2 : Vérifier les conteneurs existants

```cmd
# Voir tous les conteneurs
docker ps -a

# Voir les images
docker images
```

### Étape 3 : Vérifier les logs Docker Desktop

1. **Ouvrir Docker Desktop**
2. **Aller dans "Troubleshoot" (Dépannage)**
3. **Cliquer sur "Get Support" (Obtenir de l'aide)**
4. **Vérifier les logs**

---

## 🚀 Démarrage rapide (sans Docker)

Si Docker ne fonctionne pas, voici comment démarrer sans Docker :

### Option 1 : Scripts Batch (recommandé)

```cmd
cd C:\Users\Borel\Desktop\projetmecano\notification-service
start-services.bat
```

Cela démarre :
- Discovery Server
- Config Server
- API Gateway
- Notification Service

**Note** : PostgreSQL et RabbitMQ nécessitent toujours Docker.

### Option 2 : Démarrage manuel sans Docker

Si vous n'avez pas Docker, vous pouvez installer PostgreSQL et RabbitMQ localement :

#### PostgreSQL local
1. Télécharger PostgreSQL : https://www.postgresql.org/download/windows/
2. Installer avec les paramètres par défaut
3. Créer la base de données :
   ```sql
   CREATE DATABASE mecano_notif_db;
   ```

#### RabbitMQ local
1. Télécharger RabbitMQ : https://www.rabbitmq.com/download.html
2. Installer et démarrer
3. Accéder à http://localhost:15672 (guest/guest)

---

## 📋 Checklist de résolution

- [ ] Docker Desktop est installé
- [ ] Docker Desktop est démarré (icône verte)
- [ ] `docker --version` fonctionne
- [ ] `docker ps` fonctionne
- [ ] WSL2 est installé (si applicable)
- [ ] Aucune erreur dans Docker Desktop
- [ ] Essayer `docker-compose up --build` après redémarrage

---

## 🆘 Si rien ne fonctionne

### Alternative 1 : Utiliser les scripts Batch
```cmd
cd notification-service
start-services.bat
```

### Alternative 2 : Démarrer les services individuellement
```cmd
# Terminal 1 - PostgreSQL (si installé localement)
# Pas besoin si vous utilisez Docker pour DB seulement

# Terminal 2 - RabbitMQ (si installé localement)
# Pas besoin si vous utilisez Docker pour RabbitMQ seulement

# Terminal 3 - Discovery Server
cd discovery-server
.\mvnw.cmd spring-boot:run

# Terminal 4 - Config Server
cd config-server
.\mvnw.cmd spring-boot:run

# Terminal 5 - API Gateway
cd api-gateway
.\mvnw.cmd spring-boot:run

# Terminal 6 - Notification Service
cd notification-service
.\mvnw.cmd spring-boot:run
```

---

## 📞 Support Docker

Si le problème persiste :

1. **Vérifier les logs Docker Desktop** : Settings > Troubleshoot > Get Support
2. **Mettre à jour Docker Desktop** : Télécharger la dernière version
3. **Réinstaller Docker Desktop** :
   - Désinstaller
   - Redémarrer Windows
   - Réinstaller la dernière version

**Site officiel** : https://www.docker.com/products/docker-desktop

---

## ✅ Vérification finale

Après résolution, vérifier :

```cmd
# 1. Docker fonctionne
docker ps

# 2. Tous les services sont UP
curl http://localhost:8761 (Eureka)
curl http://localhost:8080 (API Gateway)
curl http://localhost:8085/api/notifications/health (Notification Service)

# 3. RabbitMQ UI
Start-Process "http://localhost:15672"
```

**Bon diagnostic ! 🔧**