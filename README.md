# PROJET MECANO

Plateforme de mise en relation entre propriétaires de véhicules et mécaniciens, basée sur une architecture microservices Spring Boot.

## 🏗️ Architecture

| Service | Port | Description |
|---|---|---|
| **api-gateway** | `8080` | Passerelle centrale (Spring Cloud Gateway) |
| **auth-service** | `8081` | Authentification et autorisation |
| **user-service** | `8082` | Gestion des utilisateurs |
| **geolocation-service** | `8083` | Géolocalisation des mécaniciens |
| **subscription-service** | `8084` | Abonnements et paiements (Stripe) |
| **notification-service** | `8085` | Notifications email et push |
| **repair-service** | `8086` | Gestion des réparations |
| **messaging-service** | `8087` | Messagerie entre utilisateurs (RabbitMQ) |
| **admin-service** | `8088` | Administration |
| **marketplace-service** | `8089` | Marketplace de pièces / services |
| **discovery-server** | `8761` | Service registry (Eureka) |
| **config-server** | `8888` | Configuration centralisée |

### Infrastructure

- **PostgreSQL 15** — Base de données relationnelle (port `5433`)
- **RabbitMQ 3** — Message broker (port `5673`, management UI sur `15673`)

## ✅ Prérequis

- [Docker](https://docs.docker.com/get-docker/) et Docker Compose (v2+)
- [Java 21](https://adoptium.net/) (pour le développement hors Docker)
- [Maven 3.8+](https://maven.apache.org/) (pour le développement hors Docker)

## 🚀 Démarrage rapide

### 1. Cloner le projet

```bash
git clone https://github.com/kenfackjunior183-creator/projetmecano.git
cd projetmecano
```

### 2. Travailler avec Git

```bash
# Créer et basculer sur ta branche personnelle
git checkout  <nom-de-ta-branche>

# ... faire tes modifications ...

# Ajouter et commiter
git add .
git commit -m "Description de tes modifications"

# Pousser UNIQUEMENT ta branche
git push origin <nom-de-ta-branche>
```

> ⚠️ Pousse uniquement ta branche, jamais directement sur `main`.

### 3. Lancer avec Docker Compose

```bash
docker compose up --build
```

L'ensemble de la stack démarre automatiquement (base de données, RabbitMQ, discovery server, config server, puis tous les microservices).

### 4. Services exposés

| Service | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Discovery Server (Eureka) | http://localhost:8761 |
| Config Server | http://localhost:8888 |
| RabbitMQ Management UI | http://localhost:15673 |

## 🧪 Collection Postman

Une collection Postman est fournie pour tester les API : `Mecano_API_Postman_Collection.json`

Importe-la dans [Postman](https://www.postman.com/) pour accéder à tous les endpoints.

## 📦 Bases de données

PostgreSQL est initialisé automatiquement avec les bases suivantes (via `docker/postgres-init/init.sql`) :

- `mecano_auth_db`
- `mecano_users_db`
- `mecano_geo_db`
- `mecano_billing_db`
- `mecano_notif_db`
- `mecano_repair_db`
- `mecano_marketplace_db`
- `mecano_admin_db`

## 🛠️ Développement

Chaque service peut être lancé individuellement avec Maven :

```bash
cd <service-name>
./mvnw spring-boot:run
```

Assure-toi que PostgreSQL et RabbitMQ sont accessibles et que les variables d'environnement sont correctement configurées (voir `docker-compose.yml`).

## 📂 Structure du projet

```
projetmecano/
├── admin-service/
├── api-gateway/
├── auth-service/
├── config-server/
├── discovery-server/
├── geolocation-service/
├── marketplace-service/
├── messaging-service/
├── notification-service/
├── repair-service/
├── subscription-service/
├── user-service/
├── docker/
│   └── postgres-init/
│       └── init.sql
├── docker-compose.yml
├── Mecano_API_Postman_Collection.json
└── README.md
