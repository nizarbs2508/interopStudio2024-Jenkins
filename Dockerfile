FROM jenkins/jenkins:lts
USER root
RUN apt-get update && apt-get install -y openjdk-19-jdk
ENV JAVA_HOME=/usr/lib/jvm/java-19-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH
USER jenkins

FROM openjdk:19-jdk-slim
WORKDIR /app
COPY InteropStudio.jar InteropStudio.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
