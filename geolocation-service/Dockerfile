# ============================================================
# GEOLOCATION-SERVICE - Dockerfile optimisé
# ============================================================

# ---------- ÉTAPE 1 : BUILD ----------
FROM maven:3.9-eclipse-temurin-21 AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier le pom.xml (optimisation du cache)
COPY pom.xml .

# Télécharger les dépendances une fois pour toutes
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Construire le JAR (sans les tests)
RUN mvn clean package -DskipTests

# Afficher le contenu de target pour debug
RUN ls -la /app/target/

# ---------- ÉTAPE 2 : EXÉCUTION ----------
FROM eclipse-temurin:21-jre-jammy

# Variables d'environnement
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE="docker"

# Créer un utilisateur non-root (sécurité)
RUN addgroup --system --gid 1001 spring && \
    adduser --system --uid 1001 --gid 1001 spring

# Définir le répertoire de travail
WORKDIR /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/geolocation-service-*.jar app.jar

# Créer un volume pour les logs
VOLUME /app/logs

# Changer l'utilisateur pour la sécurité
USER spring

# Exposer le port du service
EXPOSE 8083

# Point d'entrée avec les options JVM
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]