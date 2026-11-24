
# =========================
# BUILD STAGE
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy Maven configuration
COPY pom.xml .
COPY settings.xml /root/.m2/settings.xml

# Download dependencies first
RUN mvn -B dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN mvn -B -DskipTests package

# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create upload directory and configure permissions
RUN mkdir -p /app/uploads && chown -R 10001:10001 /app

# Copy executable JAR
COPY --from=build --chown=10001:10001 /workspace/target/*.jar /app/app.jar

# Run as non-root user
USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
