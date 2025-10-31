# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache deps
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# Build
COPY src ./src
RUN mvn -B -DskipTests clean install && \
    set -eux; \
    jarfile="$(ls target/*.jar | head -n 1)"; \
    cp "$jarfile" target/optimal.jar

# ---- Runtime stage ----
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /workspace/target/optimal.jar /app/optimal.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/optimal.jar"]
