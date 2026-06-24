# PROJETMECANO

Ce projet est désormais préparé pour l'exécution avec Docker et Terraform.

## Docker Compose

Lancer la stack Docker complète :

```bash
docker compose up --build
```

Services exposés :
- `http://localhost:8080` → API Gateway
- `http://localhost:8081` → Auth Service
- `http://localhost:8082` → User Service
- `http://localhost:8083` → Geolocation Service
- `http://localhost:8084` → Subscription Service
- `http://localhost:8085` → Notification Service
- `http://localhost:8761` → Discovery Server
- `http://localhost:8888` → Config Server
- `http://localhost:15672` → RabbitMQ Management

## Terraform

Le dossier `terraform/` contient une configuration Docker Terraform pour construire et lancer les mêmes services.

Utilisation :

```bash
cd terraform
terraform init
terraform apply
```

Détruire l'infrastructure :

```bash
terraform destroy
```

## Notes

- PostgreSQL initialise automatiquement les bases de données requises à partir de `docker/postgres-init/init.sql`.
- Les services Spring Boot utilisent désormais des variables d'environnement pour la configuration Docker, l'adresse du config server et le discovery server.
