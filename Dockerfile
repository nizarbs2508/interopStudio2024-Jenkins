FROM jenkins/jenkins:lts

USER root

# Install OpenJDK 19
RUN apt-get update && \
    apt-get install -y openjdk-19-jdk && \
    apt-get clean;

# Set JAVA_HOME for Jenkins
ENV JAVA_HOME=/usr/lib/jvm/java-19-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

USER jenkins


