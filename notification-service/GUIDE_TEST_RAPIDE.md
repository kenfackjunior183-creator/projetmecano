# 🚀 Guide Rapide - Tester Notification Service avec Postman (Windows)

## 📋 Étapes à suivre

### 1. Démarrer les services nécessaires

Ouvrez PowerShell **en tant qu'administrateur** et exécutez :

```powershell
# Démarrer RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management

# Démarrer PostgreSQL
docker run -d --name postgres -p 5432:5432 `
  -e POSTGRES_DB=mecano_notif_db `
  -e POSTGRES_USER=postgres `
  -e POSTGRES_PASSWORD=sage237@ `
  postgres:15
```

Puis dans des terminaux séparés :

```powershell
# Terminal 2 - Discovery Server
cd discovery-server
.\mvnw spring-boot:run

# Terminal 3 - Config Server
cd config-server
.\mvnw spring-boot:run

# Terminal 4 - API Gateway
cd api-gateway
.\mvnw spring-boot:run

# Terminal 5 - Auth Service (pour obtenir un token)
cd auth-service
.\mvnw spring-boot:run

# Terminal 6 - Notification Service
cd notification-service
.\mvnw spring-boot:run
```

### 2. Vérifier que les services sont UP

- **Eureka Dashboard** : http://localhost:8761
- **RabbitMQ Management** : http://localhost:15672 (login: `guest` / password: `guest`)
- **API Gateway** : http://localhost:8080
- **Notification Service Health** : http://localhost:8080/api/notifications/health

### 3. Importer la collection Postman

1. Ouvrez Postman
2. Cliquez sur **Import**
3. Sélectionnez le fichier `Mecano_API_Postman_Collection.json` (à la racine du projet)
4. Cliquez sur **Import**

### 4. Configurer l'environnement Postman

1. Dans Postman, cliquez sur **Environnements** (icône d'engrenage)
2. Cliquez sur **Créer un environnement**
3. Nom : `Mecano Dev`
4. Ajoutez ces variables :

| Variable | Valeur initiale |
|----------|----------------|
| `gateway` | `http://localhost:8080` |
| `accessToken` | *(vide)* |
| `authUserId` | *(vide)* |
| `userId` | *(vide)* |
| `mechanicId` | *(vide)* |
| `automobilistId` | *(vide)* |
| `repairRequestId` | *(vide)* |

5. Cliquez sur **Enregistrer**
6. Sélectionnez l'environnement `Mecano Dev` dans le menu déroulant en haut à droite

### 5. Obtenir un token JWT (OBLIGATOIRE)

Le notification-service est protégé par JWT. Vous devez d'abord vous authentifier :

1. Dans la collection, allez dans **🔐 Auth Service** → **Register – Utilisateur**
2. Cliquez sur **Send**
3. ✅ Cela crée un utilisateur et sauvegarde automatiquement le `accessToken` et `authUserId`

**OU** si vous avez déjà un compte :

1. Allez dans **🔐 Auth Service** → **Login**
2. Cliquez sur **Send**
3. ✅ Le token est sauvegardé automatiquement

### 6. Tester les endpoints du Notification Service

Maintenant, allez dans la section **🔔 Notification Service** de la collection :

#### ✅ Test 1 : Health Check (public)
- **Request** : `GET {{gateway}}/api/notifications/health`
- **Action** : Cliquez sur **Send**
- **Résultat attendu** : `Notification Service UP ✅` (status 200)

#### ✅ Test 2 : Créer un avis (Review)
**Prérequis** : Avoir un `userId`, `mechanicId` et `repairRequestId` (voir section 7 ci-dessous)

- **Request** : `POST {{gateway}}/api/notifications/reviews`
- **Headers** : Automatique (grâce au pré-request script)
- **Body** :
```json
{
  "repairRequestId": "{{repairRequestId}}",
  "automobilistId": "{{automobilistId}}",
  "mechanicId": "{{mechanicId}}",
  "rating": 5,
  "comment": "Excellent mécanicien, travail soigné !"
}
```
- **Action** : Cliquez sur **Send**
- **Résultat attendu** : Status 201 Created avec l'avis créé

#### ✅ Test 3 : Récupérer les avis d'un mécanicien
- **Request** : `GET {{gateway}}/api/notifications/reviews/mechanic/{{mechanicId}}`
- **Action** : Cliquez sur **Send**
- **Résultat attendu** : Liste des avis du mécanicien (status 200)

#### ✅ Test 4 : Récupérer les avis d'un automobiliste
- **Request** : `GET {{gateway}}/api/notifications/reviews/automobilist/{{automobilistId}}`
- **Action** : Cliquez sur **Send**
- **Résultat attendu** : Liste des avis de l'automobiliste (status 200)

#### ✅ Test 5 : Récupérer un avis par demande de réparation
- **Request** : `GET {{gateway}}/api/notifications/reviews/repair/{{repairRequestId}}`
- **Action** : Cliquez sur **Send**
- **Résultat attendu** : L'avis correspondant (status 200) ou 404 si aucun avis

### 7. Créer les données de test (si nécessaire)

Si vous n'avez pas encore de `userId`, `mechanicId`, etc., suivez ces étapes :

#### Étape 7.1 : Créer un utilisateur
1. **🔐 Auth Service** → **Register – Utilisateur**
2. **Send** → Copiez le `authUserId` retourné
3. **👤 User Service** → **Créer un utilisateur**
   - Body : `{"authUserId": "{{authUserId}}", "name": "Test User", "email": "test@mecano.com"}`
4. **Send** → Copiez le `userId` retourné

#### Étape 7.2 : Créer un profil mécanicien
1. **🔐 Auth Service** → **Upgrade Role – USER → MECHANIC**
   - Body : `{"userId": "{{authUserId}}", "newRole": "MECHANIC"}`
2. **Send**
3. **🔧 User Service – Mécaniciens** → **Créer un profil mécanicien**
   - Body : 
```json
{
  "authUserId": "{{authUserId}}",
  "userId": "{{userId}}",
  "firstName": "Jean",
  "lastName": "Dupont",
  "email": "jean.dupont@mecano.com",
  "phone": "+237690000001",
  "garageName": "Garage Dupont",
  "garageAddress": "123 Rue de la Mécanique, Bafoussam",
  "specialities": "Freins, Moteur",
  "justificationDocument": "/docs/justificatif.pdf"
}
```
4. **Send** → Copiez le `mechanicId` retourné

#### Étape 7.3 : Créer un profil automobiliste
1. **🔐 Auth Service** → **Register – Utilisateur** (pour un 2ème utilisateur)
2. **Send** → Nouveau `authUserId`
3. **👤 User Service** → **Créer un utilisateur**
4. **Send** → Nouveau `userId`
5. **🔐 Auth Service** → **Upgrade Role – USER → AUTOMOBILIST**
6. **Send**
7. **🚗 User Service – Automobilistes** → **Créer un profil automobiliste**
   - Body :
```json
{
  "authUserId": "{{authUserId}}",
  "userId": "{{userId}}",
  "firstName": "Marie",
  "lastName": "Martin",
  "email": "marie.martin@mecano.com",
  "phone": "+237691000001",
  "vehicleBrand": "Toyota",
  "vehicleModel": "Corolla",
  "vehiclePlate": "LT-1234-A",
  "drivingLicenseDocument": "/docs/permis.pdf"
}
```
8. **Send** → Copiez le `automobilistId` retourné

### 8. Tester les événements RabbitMQ (Emails)

Les notifications par email sont déclenchées par des événements RabbitMQ.

#### Via RabbitMQ Management UI :

1. Ouvrez http://localhost:15672
2. Login : `guest` / Password : `guest`
3. Allez dans l'onglet **Queues**
4. Vous devriez voir ces queues :
   - `queue.user.registered`
   - `queue.payment.confirmed`
   - `queue.repair.requested`
   - `queue.subscription.expired`

#### Publier un événement de test :

1. Cliquez sur une queue (ex: `queue.user.registered`)
2. Cliquez sur **Publish Message**
3. **Payload** (JSON) :
```json
{
  "email": "test@example.com",
  "firstName": "Jean",
  "role": "AUTOMOBILIST"
}
```
4. **Properties** : `content_type: application/json`
5. Cliquez sur **Publish Message**

**Résultat** : 
- Log dans le notification-service : `📨 Event reçu : UserRegistered → test@example.com`
- Email envoyé (vérifiez les logs du service)

### 9. Vérifier les logs du Notification Service

Dans le terminal où tourne le notification-service, vous devriez voir des logs comme :

```
📨 Event reçu : UserRegistered → test@example.com
✅ Email envoyé à test@example.com
```

### 10. Checklist de test rapide

- [ ] Health check : GET /api/notifications/health → 200
- [ ] Login réussi → Token JWT obtenu
- [ ] Création d'une review → 201 Created
- [ ] Récupération reviews par mécanicien → 200
- [ ] Récupération reviews par automobiliste → 200
- [ ] Événement RabbitMQ publié → Email envoyé

## 🐛 Dépannage rapide

### Erreur 401 Unauthorized
→ Vérifiez que vous avez bien fait le Login/Register et que le token est sauvegardé

### Erreur 404 Not Found
→ Vérifiez que tous les services sont démarrés (Eureka, API Gateway, Notification Service)

### RabbitMQ ne répond pas
→ Vérifiez avec `docker ps` que le conteneur rabbitmq est running

### Emails non envoyés
→ Vérifiez les logs du notification-service pour voir les erreurs SMTP

## 📞 URLs importantes

- **API Gateway** : http://localhost:8080
- **RabbitMQ Management** : http://localhost:15672
- **Eureka Dashboard** : http://localhost:8761
- **Notification Service Direct** : http://localhost:8085

## 🎯 Commandes utiles (PowerShell)

```powershell
# Voir les conteneurs Docker
docker ps

# Voir les logs du notification-service
docker-compose logs -f notification-service

# Redémarrer le notification-service
docker-compose restart notification-service

# Arrêter tous les services
docker-compose down
```

---

**Bon test ! 🎉**

Pour plus de détails, consultez `TESTING_GUIDE_POSTMAN.md`