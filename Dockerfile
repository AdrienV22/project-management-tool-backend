# Image Java 17 officielle
FROM eclipse-temurin:17-jdk-jammy

# Crée un volume temporaire
VOLUME /tmp

# Copie du JAR généré par Maven dans le conteneur
COPY target/project-management-tool-0.0.1-SNAPSHOT.jar app.jar

# Lance l'application avec Java
ENTRYPOINT ["java", "-jar", "/app.jar"]
