# Étape 1 : Build de l'application avec Maven et Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .

COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Exécution de l'application avec Java 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]