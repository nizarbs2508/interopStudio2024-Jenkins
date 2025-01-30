# Use the base Jenkins image
FROM jenkins/jenkins:lts

# Install dependencies and Java 19
USER root
RUN apt-get update && apt-get install -y wget curl \
    && wget https://github.com/adoptium/temurin19-binaries/releases/latest/download/OpenJDK19U-jdk_x64_linux_hotspot.tar.gz \
    && tar -xvf OpenJDK19U-jdk_x64_linux_hotspot.tar.gz \
    && mv jdk-19* /opt/java/openjdk \
    && apt-get clean

# Set JAVA_HOME and update PATH
ENV JAVA_HOME=/opt/java/openjdk/jdk-19
ENV PATH=$JAVA_HOME/bin:$PATH

# Switch back to jenkins user
USER jenkins



