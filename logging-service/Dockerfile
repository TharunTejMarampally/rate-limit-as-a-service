# ============================================
# 1️⃣ BUILD STAGE — compile all modules
# ============================================
FROM gradle:8.8-jdk17-alpine AS builder

WORKDIR /app

# Copy the whole project including root settings.gradle
COPY . .

# Run Gradle build from the root
RUN gradle :logging-service:clean :logging-service:bootJar --no-daemon --stacktrace

# ============================================
# 2️⃣ RUNTIME STAGE — lightweight image
# ============================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy only the built JAR
COPY --from=builder /app/logging-service/build/libs/*.jar app.jar

# Environment variables
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=pro

EXPOSE 8001

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
