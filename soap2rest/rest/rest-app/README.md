# rest-app

## Purpose
`rest-app` is the REST backend for the `soap2rest` example.  
It provides account-scoped APIs for:
- electric metrics
- gas metrics
- combined smart metrics
- account meters

The module is intended for learning and integration demos, including Swagger/OpenAPI exploration, API-key security, and Cucumber API scenarios.

## Architecture
The module follows a layered structure:
- `rest` package: HTTP controllers, request/response mapping.
- `service` package: business logic and validation.
- `dao` package: persistence and queries.
- `dao.entity` package: JPA entities.
- `security` + `conf` packages: authentication filter and security wiring.

Data flow:
1. Controller validates/marshals HTTP input.
2. Service enforces business rules (meter-account linking, metric ordering checks).
3. DAO executes database operations.
4. Response is returned as JSON.

## Tech Stack
- Java 21+
- Spring Boot (Web, Data JPA, Security)
- Springdoc OpenAPI (Swagger UI)
- Liquibase
- H2 in-memory database
- JUnit 5 + Mockito
- Cucumber + RestAssured

## Runtime Configuration
Key defaults from `application.yaml`:
- HTTP port: `8081`
- DB URL: `jdbc:h2:mem:soap2rest;MODE=Oracle;`
- H2 console: `/console`
- Liquibase changelog: `classpath:/liquibase/v1/changelog.xml`

## Main API Areas
- `GET /api/v1/smart`
- `GET /api/v1/smart/{id}`
- `GET /api/v1/smart/{id}/latest`
- `PUT /api/v1/smart/{id}`
- `DELETE /api/v1/smart/{id}`

- `GET /api/v1/smart/{id}/electric`
- `GET /api/v1/smart/{id}/electric/latest`
- `PUT /api/v1/smart/{id}/electric`
- `DELETE /api/v1/smart/{id}/electric`

- `GET /api/v1/smart/{id}/gas`
- `GET /api/v1/smart/{id}/gas/latest`
- `PUT /api/v1/smart/{id}/gas`
- `DELETE /api/v1/smart/{id}/gas`

- `GET /api/v1/smart/{id}/meters`
- `GET /api/v1/smart/{id}/meters/{meterId}`
- `PUT /api/v1/smart/{id}/meters`
- `PUT /api/v1/smart/{id}/meters/{meterId}`
- `DELETE /api/v1/smart/{id}/meters`
- `DELETE /api/v1/smart/{id}/meters/{meterId}`

## Build and Tests
From repository root:

```bash
mvn -pl soap2rest/rest/rest-app test
```

Note: `maven-surefire-plugin` in this module is configured to run Cucumber runner classes.

## Start Application
From repository root:

```bash
mvn -pl soap2rest/rest/rest-app -am spring-boot:run
```

Swagger UI:
- <http://localhost:8081/swagger-ui/index.html>

OpenAPI JSON:
- <http://localhost:8081/v3/api-docs>

H2 Console:
- <http://localhost:8081/console>

H2 login values:
- JDBC URL: `jdbc:h2:mem:soap2rest;MODE=Oracle;DB_CLOSE_DELAY=-1`
- User: `sa`
- Password: `sa`

## Security Modes and Browser Access

### 1) Start with project security (API key filter)
Current default in this module uses custom API-key security.

How to open Swagger/H2 in browser:
1. Install a header extension (for example, ModHeader or Requestly).
2. Add rule for `http://localhost:8081/*`:
   - header: `X-API-KEY`
   - value: one key from `src/main/resources/api.keys` (for example `57AkjqNuz44QmUHQuvVo`)
3. Open:
   - <http://localhost:8081/swagger-ui/index.html>
   - <http://localhost:8081/console>

### 2) Spring Boot default user mode (generated password)
This mode is not the project default, but can be useful for troubleshooting.

To use it:
1. Disable custom security config (comment `@Configuration` and `@EnableWebSecurity` in `SecurityConfiguration`).
2. Start app.

Then Spring Boot creates:
- username: `user`
- password: generated at startup log line:
  - `Using generated security password: ...`

#### Drawbacks
You cannot login into /console.

It throws 403 exception.

That 403 happens because you’re on Spring Boot default security flow, and H2 console login POST gets blocked by CSRF.

So use either 1 or 3 option.

### 3) No security mode (Swagger and /console without auth)
To run with all security disabled:
1. Keep `Application` excluding `SecurityAutoConfiguration`.
2. Disable custom security config (comment `@Configuration` and `@EnableWebSecurity` in `SecurityConfiguration`).
3. Add 
```java
@SpringBootApplication(scanBasePackages = {
        "my.javacraft.soap2rest.utils",
        "my.javacraft.soap2rest.rest.app",
}, exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
```
4. Start app with:

```bash
mvn -pl soap2rest/rest/rest-app -am spring-boot:run
```

In this mode, Swagger and H2 console are accessible directly:
- <http://localhost:8081/swagger-ui/index.html>
- <http://localhost:8081/console>

## Troubleshooting
- `401 Invalid API Key`: missing/invalid `X-API-KEY` header.
- `403` on H2 console login: you are likely in default Spring Security mode with CSRF; use project API-key mode or full no-security mode.
- `Connection refused`: verify app is running and port `8081` is free.
