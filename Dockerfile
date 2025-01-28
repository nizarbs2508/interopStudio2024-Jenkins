# Étape de build
FROM maven:3.8.4-openjdk-11-slim AS build

WORKDIR /app

# Copier le fichier pom.xml et les sources Maven
COPY pom.xml .
RUN mvn dependency:go-offline

# Copier le code source et construire l'application JavaFX
COPY src ./src
RUN mvn clean install assembly:single -DskipTests


# Use OpenJDK base image with Java 19
FROM eclipse-temurin:19-jre

# Variables d'environnement pour JavaFX
ENV DISPLAY=:0

# Installer les dépendances X11 pour l'affichage graphique
RUN apt-get update && apt-get install -y \
    libxext6 \
    libxrender1 \
    libxtst6 \
    && rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /app

# Copier l'artefact JAR généré par Maven
COPY --from=build /app/target/InteropStudio.jar /app/InteropStudio.jar

# Commande pour lancer l'application JavaFX
ENTRYPOINT ["java", "-jar", "InteropStudio.jar"]