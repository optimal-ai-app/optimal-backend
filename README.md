# Spring Boot Boilerplate
 *Spring Boot Boilerplate* is a **starter kit**. This project is simple and useful.
 
## Development Workflow

### Initial Setup
```bash
# 1. Backend Setup
cd optimal-backend
mvn clean install
mvn clean compile && mvn spring-boot:run 

# 2. Start ngrok in new terminal
cd optimal  # Go to project root
ngrok http http://localhost:8080/

# 3. Update API URL in frontend
# Copy ngrok Forwarding URL (e.g., https://your-tunnel.ngrok-free.app)
# Update in optimal/services/httpService.ts

# 4. Start frontend in new terminal
cd optimal
npm install
npx expo start
```

### Making Changes

#### Backend Changes
- Keep ngrok running (don't restart unless you stop it)
- Backend has hot-reload enabled:
  - Save any Java file
  - Watch terminal for "Restarting due to changes..."
  - Wait ~2-3 seconds for restart
  - No need to restart ngrok or Expo

#### Frontend Changes
- Keep ngrok and backend running
- After editing frontend code:
  - Press 'r' in Expo terminal to reload app, or
  - Save changes and wait for auto-reload

### When to Restart What

Never need to restart:
- ngrok (unless you stop it)
- Expo (unless you change dependencies)

Backend auto-restarts when:
- You save a Java file
- DevTools detects changes

Frontend auto-reloads when:
- You save TypeScript/React files
- Or press 'r' in Expo terminal

Must manually restart if you:
- Change dependencies (package.json/pom.xml)
- Stop ngrok (need new URL)
- Change Spring configs
- Add new Maven dependencies

### Testing Flow
1. Make backend changes → Save → Wait for auto-restart
2. Make frontend changes → Save → Wait for auto-reload (or press 'r')
3. Test in simulator
4. If it works, commit changes

## Technologies 
- Spring Boot (v3.4.0)
- Spring Data JPA
- Spring Validation
- Spring Security + JWT Token
- PostgreSQL
- Mapstruct
- Lombok
- Swagger (Open API)

## Customization

- You can customize ```token information (secret key, issuer, expiry date) ``` in [*application.yml*](https://github.com/Genc/spring-boot-boilerplate/blob/master/src/main/resources/application.yml#L40) file.
- You can customize ```database connection information``` in [*application.yml*](https://github.com/Genc/spring-boot-boilerplate/blob/master/src/main/resources/application.yml#L3) file.
- You can customize ```swagger information``` in [*application.yml*](https://github.com/Genc/spring-boot-boilerplate/blob/master/src/main/resources/application.yml#L45) file.
- You can customize ```which endpoints are accessible without token information``` in [*SecurityConfiguration.java*](https://github.com/Genc/spring-boot-boilerplate/blob/master/src/main/java/com/farukgenc/boilerplate/springboot/configuration/SecurityConfiguration.java#L45) file.

## Run the Application

First you need to make sure that the database is up. 
If you're using Docker, you can use ```docker compose up -d``` command. (If you have made changes in local, you should use the *local-docker-compose* file.)

Navigate to the root of the project. For building the project using command line, run below command :

``` mvn clean install```

Run service in command line. Navigate to *target* directory. 

``` java -jar spring-boot-boilerplate.jar ```

## Postman Collection

- [You can access the Postman collection here and you can try it after you get the project up and running.](https://www.postman.com/postmanfaruk/workspace/faruk-genc-projects/collection/11439300-3d0317df-f217-40ff-a2a6-4eaaf66e1c55?action=share&creator=11439300)

### Others

 - [For Angular]
 
### License

Apache License 2.0

   [For Angular]: <https://github.com/Genc/angular-boilerplate>