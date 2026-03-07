# soap2rest/rest

`soap2rest/rest` contains the REST contract and REST implementation used by the SOAP-to-REST demo.

## Contents
- [Purpose](#purpose)
- [Module Structure](#module-structure)
- [Architecture](#architecture)
- [Data and Migrations](#data-and-migrations)
- [Tech Stack](#tech-stack)
- [Build and Test](#build-and-test)
- [Run and Access](#run-and-access)

## Purpose
This parent module separates:
- API models that can be shared (`rest-api`)
- runnable REST service with controllers, business logic, persistence, security, and BDD tests (`rest-app`)

## Module Structure
- `rest-api`
  - shared API classes (`Metric`, `Metrics`)
  - no web runtime
- `rest-app`
  - Spring Boot application
  - controllers for smart/electric/gas/meter flows
  - Liquibase-managed schema and seed data
  - Swagger UI + H2 console
  - API-key based security setup

Detailed operational guide:
- [`rest-app/README.md`](rest-app/README.md)

## Architecture
`rest-app` follows a layered structure:
1. `rest` controllers: HTTP mapping + response shaping.
2. `service` layer: validation and business rules.
3. `dao` layer: JPA repositories and queries.
4. `dao.entity`: persistence entities.

This keeps HTTP concerns separate from business logic and DB access.

## Data and Migrations
Database is managed by Liquibase changelog files in:
- `rest-app/src/main/resources/liquibase/v1/`

Runtime uses H2 in-memory DB by default.

Metric uniqueness is meter-scoped:
- gas metrics: unique on `(meter_id, date)`
- electric metrics: unique on `(meter_id, date)`

This allows different meters to submit readings on the same day.

## Tech Stack
- Java + Maven
- Spring Boot (Web, Data JPA, Security)
- Liquibase
- H2
- Springdoc OpenAPI
- JUnit 5 + Mockito
- Cucumber + RestAssured

## Build and Test
From repository root:

Run `rest-api` tests:
```bash
mvn -pl soap2rest/rest/rest-api test
```

Run `rest-app` tests:
```bash
mvn -pl soap2rest/rest/rest-app test
```

## Run and Access
Start `rest-app`:
```bash
mvn -pl soap2rest/rest/rest-app -am spring-boot:run
```

Default URLs:
- Swagger UI: <http://localhost:8081/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8081/v3/api-docs>
- H2 Console: <http://localhost:8081/console>

Security modes (API key mode, default user/password mode, and no-security mode) are documented in:
- [`rest-app/README.md`](rest-app/README.md)
