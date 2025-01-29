FROM jenkins/jenkins:lts
USER root

# Install OpenJDK 19
RUN apt-get update && apt-get install -y openjdk-19-jdk

# Set JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-19-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

USER jenkins


FROM openjdk:19-jdk-slim
WORKDIR /app
COPY InteropStudio.jar InteropStudio.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
