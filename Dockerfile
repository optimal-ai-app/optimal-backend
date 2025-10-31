FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM maven:3.9-eclipse-temurin-17
WORKDIR /app
COPY --from=build /app/target/*.jar optimal.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","optimal.jar"]