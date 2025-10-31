FROM eclipse-temurin:17
WORKDIR app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} optimal.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","optimal.jar"]