# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache deps
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# Build and ensure target/optimal.jar exists exactly once
COPY src ./src
RUN set -euo pipefail; \
    mvn -B -DskipTests clean install; \
    if [ -f target/optimal.jar ]; then \
      echo "optimal.jar already exists"; \
    else \
      jarfile="$(find target -maxdepth 1 -type f -name '*.jar' ! -name 'optimal.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' | head -n 1)"; \
      [ -n "$jarfile" ] || { echo 'No runnable JAR found in target/'; exit 1; }; \
      cp "$jarfile" target/optimal.jar; \
    fi

# ---- Runtime stage ----
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /workspace/target/optimal.jar /app/optimal.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/optimal.jar"]
