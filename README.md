# Optimal - Backend Repository
## Website: [Optimal](https://useoptimal.app)
## Demo Video:
<div>
  <a href="https://www.loom.com/share/692237f364174fa7bd52238adc1beb88">
    <p>Optimal - Your Personal AI Accountability Coach (Demo Video) - Watch Video</p>
  </a>
  <a href="https://www.loom.com/share/692237f364174fa7bd52238adc1beb88">
    <img style="max-width:300px;" src="https://cdn.loom.com/sessions/thumbnails/692237f364174fa7bd52238adc1beb88-220725109b52db1e-full-play.gif#t=0.1">
  </a>
</div>




## About
Optimal is an AI-powered accountability coaching app. This repository contains the Spring Boot backend, which powers goal creation, task scheduling, habit tracking, and an AI coaching system built on a custom multi-agent orchestration framework.

### AI Agent Architecture
The backend features a hand-rolled multi-agent orchestration system built on LangChain4j, without relying on high-level agent frameworks. A `BaseSupervisor` interprets each user message using an LLM-based router to dynamically assemble and execute agent teams from a predefined registry:

- **GoalDefinitionTeam** — `GoalCreatorAgent`
- **MilestoneExecutionTeam** — `MilestonePlannerAgent` → `TaskCreatorAgent`
- **TaskExecutionTeam** — `TaskPlannerAgent` → `TaskCreatorAgent`

Agents are executed with dependency resolution, where downstream agents receive structured context from upstream agents before running. A stateful handoff mechanism persists agent control across multi-turn conversations, enabling multi-step workflows like goal creation flowing into milestone planning and task generation without user re-prompting.

### Additional Engineering Highlights
- **Tool-augmented agents** — Each agent is equipped with LangChain4j `@Tool`-annotated tools (goal retrieval, task querying, date calculation, milestone queue management) that execute real database operations during inference
- **Guardrails** — Input and output guardrails handle prompt injection detection and JSON format validation, with an LLM-based format fixer as a fallback
- **Prompt routing** — A lightweight instruction selector runs between turns to identify the correct step in a multi-step agent prompt, reducing token waste and improving response accuracy
- **Supervisor memory management** — Per-session supervisors are stored in a `ConcurrentHashMap` with LRU-style eviction, inactivity-based TTL, and a background cleanup executor to prevent memory leaks at scale
- **Context-aware tooling** — `UserContext` uses `ThreadLocal` storage to propagate user ID, chat ID, and local date to all tools within a request thread without parameter threading

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
