FROM jenkins/jenkins:lts

USER root

# Set JAVA_HOME for the container
ENV JAVA_HOME=/usr/lib/jvm/java-19-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Install Java if necessary, or use the existing one
RUN apt-get update && apt-get install -y openjdk-19-jdk

USER jenkins



