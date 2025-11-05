# ============================================
# 1️⃣ BUILD STAGE — compile all modules
# ============================================
FROM gradle:8.8-jdk17-alpine AS builder

WORKDIR /app

# Copy the whole project including root settings.gradle
COPY . .

# Run Gradle build from the root
RUN gradle :rate-limit-service:clean :rate-limit-service:bootJar --no-daemon --stacktrace

# ============================================
# 2️⃣ RUNTIME STAGE — lightweight image
# ============================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy only the built JAR
COPY --from=builder /app/rate-limit-service/build/libs/*.jar app.jar

# Environment variables
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
