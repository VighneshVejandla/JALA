# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline -q

COPY src src
RUN ./mvnw -B package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre

RUN useradd --system --uid 1001 appuser
USER appuser

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

# SPRING_PROFILES_ACTIVE, DB_*, JWT_*, SUPABASE_* are supplied by the
# runtime environment; see .env.example.
ENTRYPOINT ["java", "-jar", "app.jar"]
