FROM openjdk:19-jdk-slim
WORKDIR /app
COPY InteropStudio.jar InteropStudio.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
