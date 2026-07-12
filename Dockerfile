# ═══════════════════════════════════════════════════════════════
# Stage 1: Build Phase
# ═══════════════════════════════════════════════════════════════
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml and fetch dependencies to leverage docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package the application jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ═══════════════════════════════════════════════════════════════
# Stage 2: Runtime Phase
# ═══════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl in the alpine JRE for health checks
RUN apk --no-cache add curl

# Create directory for uploads and assign non-root ownership
RUN mkdir -p uploads && \
    chown -R 1000:1000 /app

# Copy packaged jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Run as non-root user (security best practices)
USER 1000:1000

# Expose ports
EXPOSE 8080

# Run Spring Boot app under prod profile
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
