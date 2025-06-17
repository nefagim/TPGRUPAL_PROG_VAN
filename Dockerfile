# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk-jammy

# Set the working directory in the container
WORKDIR /app

# Arguments for user and group (optional, good practice for non-root execution)
ARG APP_USER=appuser
ARG APP_GROUP=appgroup
ARG UID=1001
ARG GID=1001

# Create a non-root user and group
RUN groupadd -g ${GID} ${APP_GROUP} && \
    useradd -u ${UID} -g ${APP_GROUP} -m -s /bin/sh ${APP_USER}

# Copy the fat jar into the container at /app
# Ensure your JAR name in pom.xml matches this.
# Typically found in target/demostockapp-0.0.1-SNAPSHOT.jar after `mvn package`
COPY target/demostockapp-0.0.1-SNAPSHOT.jar app.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Change ownership of the app directory and jar file to the non-root user
RUN chown -R ${APP_USER}:${APP_GROUP} /app

# Switch to the non-root user
USER ${APP_USER}

# Run the jar file
ENTRYPOINT ["java","-jar","/app/app.jar"]
