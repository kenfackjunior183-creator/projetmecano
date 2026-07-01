# 🧪 Guide de test du Notification Service avec Postman

## 📋 Table des matières
1. [Prérequis](#prérequis)
2. [Architecture du test](#architecture-du-test)
3. [Configuration Postman](#configuration-postman)
4. [Obtenir un token JWT](#obtenir-un-token-jwt)
5. [Tester les endpoints REST](#tester-les-endpoints-rest)
6. [Tester les événements RabbitMQ](#tester-les-événements-rabbitmq)
7. [Vérifier les emails](#vérifier-les-emails)

---

## 🔧 Prérequis

### Services à démarrer
```bash
# 1. Discovery Server (Eureka)
cd discovery-server && ./mvnw spring-boot:run

# 2. Config Server
cd config-server && ./mvnw spring-boot:run

# 3. API Gateway
cd api-gateway && ./mvnw spring-boot:run

# 4. RabbitMQ (via Docker)
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management

# 5. PostgreSQL (via Docker)
docker run -d --name postgres -p 5432:5432 \
  -e POSTGRES_DB=mecano_notif_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=sage237@ \
  postgres:15

# 6. Notification Service
cd notification-service && ./mvnw spring-boot:run
```

### Vérifier que les services sont UP
- Eureka Dashboard : http://localhost:8761
- RabbitMQ Management : http://localhost:15672 (guest/guest)
- API Gateway : http://localhost:8080
- Notification Service : http://localhost:8085/api/notifications/health

---

## 🏗️ Architecture du test

```
┌─────────────┐      ┌──────────────┐      ┌──────────────────┐
│   Postman   │─────▶│ API Gateway  │─────▶│ Notification Svc │
│             │      │  (port 8080) │      │   (port 8085)    │
└─────────────┘      └──────────────┘      └──────────────────┘
        │                     │                      │
        │                     │                      │
        ▼                     ▼                      ▼
   [Tests REST]        [JWT Validation]      [RabbitMQ / Email]
```

**Note** : Le notification-service est protégé par JWT. Toutes les requêtes (sauf `/health`) nécessitent un token valide dans le header `Authorization: Bearer <token>`.

---

## ⚙️ Configuration Postman

### 1. Créer une collection
- Nom : `Mecano - Notification Service`
- Description : Tests du microservice de notifications

### 2. Variables d'environnement
Créer un environnement `Mecano Dev` avec les variables suivantes :

| Variable | Valeur initiale | Description |
|----------|----------------|-------------|
| `base_url` | `http://localhost:8080` | URL de l'API Gateway |
| `notification_url` | `http://localhost:8085` | URL directe du notification-service (si besoin) |
| `auth_url` | `http://localhost:8080` | URL pour obtenir le JWT |
| `access_token` | *(vide)* | Token JWT (sera rempli automatiquement) |
| `mechanic_id` | *(UUID)* | ID d'un mécanicien de test |
| `automobilist_id` | *(UUID)* | ID d'un automobiliste de test |
| `repair_request_id` | *(UUID)* | ID d'une demande de réparation |

### 3. Pré-request Script (Collection)
Ajouter ce script pour injecter automatiquement le token JWT :

```javascript
// Pré-request Script de la collection
if (pm.environment.get("access_token")) {
    pm.request.headers.upsert({
        key: "Authorization",
        value: "Bearer " + pm.environment.get("access_token")
    });
}
```

---

## 🔐 Obtenir un token JWT

Le notification-service utilise l'authentification JWT. Vous devez d'abord obtenir un token via le **auth-service**.

### Option 1 : Via l'API Gateway (recommandé)

**Request** :
- **Method** : `POST`
- **URL** : `{{base_url}}/api/auth/login`
- **Headers** :
  ```
  Content-Type: application/json
  ```
- **Body** (raw JSON) :
```json
{
  "email": "test@mecano.com",
  "password": "password123"
}
```

**Response** (200 OK) :
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

**Action** : Copier la valeur de `accessToken` et la coller dans la variable Postman `access_token`.

### Option 2 : Si vous n'avez pas d'auth-service

Pour tester en développement, vous pouvez temporairement désactiver la sécurité :

```yaml
# application.yml (notification-service)
spring:
  security:
    enabled: false
```

Ou utiliser un endpoint de debug (si implémenté dans auth-service).

---

## 🧪 Tester les endpoints REST

### 1. Health Check (public)

**Request** :
- **Method** : `GET`
- **URL** : `{{base_url}}/api/notifications/health`
- **Headers** : Aucun (endpoint public)

**Response attendue** :
```
Notification Service UP ✅
```

---

### 2. Créer un avis (Review) - AUTOMOBILIST

**Request** :
- **Method** : `POST`
- **URL** : `{{base_url}}/api/notifications/reviews`
- **Headers** :
  ```
  Authorization: Bearer {{access_token}}
  Content-Type: application/json
  ```
- **Body** (raw JSON) :
```json
{
  "repairRequestId": "550e8400-e29b-41d4-a716-446655440000",
  "automobilistId": "660e8400-e29b-41d4-a716-446655440001",
  "mechanicId": "770e8400-e29b-41d4-a716-446655440002",
  "rating": 5,
  "comment": "Excellent mécanicien, travail soigné et rapide !"
}
```

**Response attendue** (201 Created) :
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440003",
  "repairRequestId": "550e8400-e29b-41d4-a716-446655440000",
  "automobilistId": "660e8400-e29b-41d4-a716-446655440001",
  "mechanicId": "770e8400-e29b-41d4-a716-446655440002",
  "rating": 5,
  "comment": "Excellent mécanicien, travail soigné et rapide !",
  "createdAt": "2025-06-25T16:30:00"
}
```

---

### 3. Récupérer les avis d'un mécanicien

**Request** :
- **Method** : `GET`
- **URL** : `{{base_url}}/api/notifications/reviews/mechanic/{{mechanic_id}}`
- **Headers** :
  ```
  Authorization: Bearer {{access_token}}
  ```

**Response attendue** (200 OK) :
```json
[
  {
    "id": "880e8400-e29b-41d4-a716-446655440003",
    "repairRequestId": "550e8400-e29b-41d4-a716-446655440000",
    "automobilistId": "660e8400-e29b-41d4-a716-446655440001",
    "mechanicId": "770e8400-e29b-41d4-a716-446655440002",
    "rating": 5,
    "comment": "Excellent mécanicien, travail soigné et rapide !",
    "createdAt": "2025-06-25T16:30:00"
  }
]
```

---

### 4. Récupérer les avis d'un automobiliste

**Request** :
- **Method** : `GET`
- **URL** : `{{base_url}}/api/notifications/reviews/automobilist/{{automobilist_id}}`
- **Headers** :
  ```
  Authorization: Bearer {{access_token}}
  ```

**Response** : Liste des avis laissés par l'automobiliste

---

### 5. Récupérer un avis par demande de réparation

**Request** :
- **Method** : `GET`
- **URL** : `{{base_url}}/api/notifications/reviews/repair/{{repair_request_id}}`
- **Headers** :
  ```
  Authorization: Bearer {{access_token}}
  ```

**Response attendue** (200 OK ou 404) :
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440003",
  "repairRequestId": "550e8400-e29b-41d4-a716-446655440000",
  "automobilistId": "660e8400-e29b-41d4-a716-446655440001",
  "mechanicId": "770e8400-e29b-41d4-a716-446655440002",
  "rating": 5,
  "comment": "Excellent mécanicien",
  "createdAt": "2025-06-25T16:30:00"
}
```

---

## 📨 Tester les événements RabbitMQ

Les notifications par email sont déclenchées par des événements RabbitMQ. Voici comment les tester :

### Prérequis pour les tests RabbitMQ
1. **RabbitMQ Management UI** : http://localhost:15672
   - Login : `guest`
   - Password : `guest`

2. **Vérifier les queues** :
   - Aller dans l'onglet **Queues**
   - Vous devriez voir 4 queues :
     - `queue.user.registered`
     - `queue.payment.confirmed`
     - `queue.repair.requested`
     - `queue.subscription.expired`
     - `queue.dlq` (Dead Letter Queue)

---

### Événement 1 : User Registered

**Objectif** : Envoyer un email de bienvenue à un nouvel utilisateur

**Méthode** : Publier un message dans RabbitMQ (via Management UI ou script)

**Via RabbitMQ Management UI** :
1. Aller dans **Queues** → `queue.user.registered`
2. Cliquer sur **Publish Message**
3. Remplir le formulaire :
   - **Payload** (JSON) :
```json
{
  "email": "nouveau.utilisateur@example.com",
  "firstName": "Jean",
  "role": "AUTOMOBILIST"
}
```
   - **Properties** : `content_type: application/json`
4. Cliquer sur **Publish Message**

**Résultat attendu** :
- Log dans le notification-service : `📨 Event reçu : UserRegistered → nouveau.utilisateur@example.com`
- Email envoyé via Mailtrap (vérifier sur https://mailtrap.io)

---

### Événement 2 : Payment Confirmed

**Objectif** : Confirmer l'activation d'un abonnement

**Via RabbitMQ Management UI** :
1. Aller dans **Queues** → `queue.payment.confirmed`
2. **Publish Message** avec le payload :
```json
{
  "email": "mecanicien@example.com",
  "firstName": "Pierre",
  "planLevel": "PREMIUM",
  "amount": "29.99",
  "currency": "EUR"
}
```

**Résultat attendu** :
- Email de confirmation d'abonnement envoyé

---

### Événement 3 : Repair Requested

**Objectif** : Notifier l'automobiliste et le mécanicien d'une demande de dépannage

**Via RabbitMQ Management UI** :
1. Aller dans **Queues** → `queue.repair.requested`
2. **Publish Message** avec le payload :
```json
{
  "automobilistEmail": "client@example.com",
  "automobilistFirstName": "Marie",
  "mechanicEmail": "mecanicien@example.com",
  "mechanicFirstName": "Thomas",
  "description": "Panne de moteur sur autoroute A1",
  "latitude": 48.8566,
  "longitude": 2.3522
}
```

**Résultat attendu** :
- **2 emails envoyés** :
  1. À l'automobiliste : confirmation d'envoi de demande
  2. Au mécanicien : nouvelle demande de dépannage avec coordonnées GPS

---

### Événement 4 : Subscription Expired

**Objectif** : Alerter un mécanicien que son abonnement a expiré

**Via RabbitMQ Management UI** :
1. Aller dans **Queues** → `queue.subscription.expired`
2. **Publish Message** avec le payload :
```json
{
  "email": "mecanicien@example.com",
  "firstName": "Thomas",
  "planLevel": "PREMIUM"
}
```

**Résultat attendu** :
- Email d'alerte avec lien de renouvellement

---

## 📬 Vérifier les emails

### Avec Mailtrap (recommandé pour les tests)

1. **Créer un compte** sur https://mailtrap.io (gratuit)
2. **Créer une inbox** et récupérer les identifiants SMTP :
   - Host : `sandbox.smtp.mailtrap.io`
   - Port : `2525`
   - Username : (généré par Mailtrap)
   - Password : (généré par Mailtrap)
3. **Mettre à jour `application.yml`** :
```yaml
spring:
  mail:
    host: sandbox.smtp.mailtrap.io
    port: 2525
    username: VOTRE_USERNAME_MAILTRAP
    password: VOTRE_PASSWORD_MAILTRAP
```
4. **Vérifier les emails** dans l'interface Mailtrap

### Avec un SMTP local (MailHog)

```bash
# Démarrer MailHog
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog

# Accéder à l'interface : http://localhost:8025
```

Mettre à jour `application.yml` :
```yaml
spring:
  mail:
    host: localhost
    port: 1025
    username: ""
    password: ""
```

---

## 🧪 Tests automatisés avec Postman

### Créer une Collection avec tous les tests

#### Étape 1 : Variables d'environnement
```
base_url: http://localhost:8080
access_token: eyJhbGciOiJIUzI1NiJ9...
mechanic_id: 770e8400-e29b-41d4-a716-446655440002
automobilist_id: 660e8400-e29b-41d4-a716-446655440001
repair_request_id: 550e8400-e29b-41d4-a716-446655440000
```

#### Étape 2 : Requêtes à ajouter

**1. Login (pour obtenir le token)**
```
POST {{base_url}}/api/auth/login
Body: { "email": "test@mecano.com", "password": "password123" }
Tests: pm.environment.set("access_token", responseBody.accessToken)
```

**2. Health Check**
```
GET {{base_url}}/api/notifications/health
Tests: pm.test("Status is 200", () => pm.response.code === 200)
```

**3. Créer une review**
```
POST {{base_url}}/api/notifications/reviews
Headers: Authorization: Bearer {{access_token}}
Body: { "repairRequestId": "{{repair_request_id}}", "automobilistId": "{{automobilist_id}}", "mechanicId": "{{mechanic_id}}", "rating": 5, "comment": "Test" }
Tests: pm.test("Status is 201", () => pm.response.code === 201)
```

**4. Récupérer les reviews d'un mécanicien**
```
GET {{base_url}}/api/notifications/reviews/mechanic/{{mechanic_id}}
Headers: Authorization: Bearer {{access_token}}
Tests: pm.test("Status is 200", () => pm.response.code === 200)
```

---

## 🐛 Dépannage

### Problème : 401 Unauthorized
**Cause** : Token JWT invalide ou absent
**Solution** :
- Vérifier que le header `Authorization: Bearer <token>` est présent
- Vérifier que le token n'a pas expiré
- Vérifier que le secret JWT est identique entre auth-service et notification-service

### Problème : 403 Forbidden
**Cause** : Token valide mais permissions insuffisantes
**Solution** : Vérifier les rôles dans le token JWT (claims)

### Problème : Connexion à RabbitMQ échouée
**Cause** : RabbitMQ non démarré ou mauvaise configuration
**Solution** :
```bash
# Vérifier que RabbitMQ est running
docker ps | grep rabbitmq

# Tester la connexion
curl http://localhost:15672
```

### Problème : Emails non envoyés
**Cause** : Configuration SMTP incorrecte
**Solution** :
- Vérifier les logs : `grep "Erreur envoi email" logs/notification-service.log`
- Tester la connexion SMTP avec Telnet :
  ```bash
  telnet sandbox.smtp.mailtrap.io 2525
  ```
- Vérifier les identifiants Mailtrap

### Problème : Messages RabbitMQ en DLQ
**Cause** : Erreur de traitement des événements
**Solution** :
- Vérifier la queue `queue.dlq` dans RabbitMQ Management
- Consulter les logs du notification-service
- Vérifier que les DTOs correspondent aux événements attendus

---

## 📊 Monitoring et logs

### Voir les logs en temps réel
```bash
# Linux/Mac
tail -f notification-service/logs/notification-service.log | grep "📨\|✅\|❌"

# Windows
Get-Content notification-service\logs\notification-service.log -Wait | Select-String "📨|✅|❌"
```

### Vérifier les queues RabbitMQ
```bash
# Via RabbitMQ Management UI
http://localhost:15672

# Via CLI
docker exec -it <rabbitmq_container> rabbitmqctl list_queues
```

### Vérifier Eureka
```bash
# Dashboard
http://localhost:8761

# Vérifier que notification-service est enregistré
```

---

## 🎯 Scénarios de test complets

### Scénario 1 : Inscription + Email de bienvenue
1. Créer un compte via auth-service (`POST /api/auth/register`)
2. Vérifier l'arrivée de l'événement `user.registered` dans RabbitMQ
3. Vérifier la réception de l'email de bienvenue

### Scénario 2 : Paiement + Confirmation
1. Simuler un paiement (via payment-service)
2. Vérifier l'événement `payment.confirmed`
3. Vérifier l'email de confirmation d'abonnement

### Scénario 3 : Demande de dépannage
1. Créer une demande de réparation (via repair-service)
2. Vérifier l'événement `repair.requested`
3. Vérifier la réception de **2 emails** (automobiliste + mécanicien)

### Scénario 4 : Système d'avis
1. Créer une review (`POST /api/notifications/reviews`)
2. Récupérer les reviews d'un mécanicien (`GET /api/notifications/reviews/mechanic/{id}`)
3. Vérifier que la note moyenne est correcte

---

## 📝 Notes importantes

1. **Authentification** : Tous les endpoints (sauf `/health`) nécessitent un JWT valide
2. **RabbitMQ** : Les événements sont consommés automatiquement par le `NotificationListener`
3. **Emails** : En production, remplacer Mailtrap par un vrai SMTP (SendGrid, AWS SES, etc.)
4. **Base URL** : Configurer `application.notification.base-url` selon l'environnement
5. **Profils Spring** : Utiliser `application-dev.yml` et `application-prod.yml` pour différencier les configs

---

## 🚀 Commandes rapides

```bash
# Démarrer tous les services (avec Docker Compose)
docker-compose up -d

# Voir les logs d'un service
docker-compose logs -f notification-service

# Redémarrer un service
docker-compose restart notification-service

# Accéder à RabbitMQ Management
open http://localhost:15672

# Tester le health check
curl http://localhost:8085/api/notifications/health

# Tester avec un token (remplacer <TOKEN>)
curl -H "Authorization: Bearer <TOKEN>" \
     http://localhost:8080/api/notifications/reviews/mechanic/<UUID>
```

---

## ✅ Checklist de test

- [ ] Health check répond 200
- [ ] Login retourne un token JWT valide
- [ ] Création d'une review fonctionne (201)
- [ ] Récupération des reviews par mécanicien (200)
- [ ] Récupération des reviews par automobiliste (200)
- [ ] Récupération d'une review par repair_request_id (200 ou 404)
- [ ] Événement `user.registered` envoie un email
- [ ] Événement `payment.confirmed` envoie un email
- [ ] Événement `repair.requested` envoie 2 emails
- [ ] Événement `subscription.expired` envoie un email
- [ ] Messages en erreur vont dans la DLQ
- [ ] Authentification JWT fonctionne (401 sans token, 200 avec token)

---

**Bon test ! 🎉**