# Build context is the repository root (see docker-compose.yml) so the image can
# bundle both the backend jar and the challenge-tests templates.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend/pom.xml .
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# CodeInjectionService reads these templates at runtime (CHALLENGE_TESTS_DIR=/challenge-tests).
COPY challenge-tests /challenge-tests
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
