FROM jenkins/jenkins:lts

USER root

# Install Java 19
RUN wget https://github.com/adoptium/temurin19-binaries/releases/latest/download/OpenJDK19U-debugimage_aarch64_linux_hotspot_19.0.2_7.tar.gz \
    && tar -xvf OpenJDK19U-debugimage_aarch64_linux_hotspot_19.0.2_7.tar.gz \
    && mv jdk-19* /opt/java/openjdk \
    && apt-get clean

# Set JAVA_HOME and PATH to use Java 19
ENV JAVA_HOME=/opt/java/openjdk/jdk-19
ENV PATH=$JAVA_HOME/bin:$PATH

USER jenkins



