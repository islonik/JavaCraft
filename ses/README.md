# Simple Event System (ses)
<sub>[Back to JavaCraft](../README.md#micro-java-samples)</sub>

Initially created in 2016 and rebuilt in-place as an event-sourced work request service.

`ses` is now a PostgreSQL-backed Spring Boot example that demonstrates:
- CQRS with separate command and query paths
- append-only event storage
- replayable SQL projections
- live projection updates over SSE
- in-process event fan-out through the preserved `ses-events` kernel

See [ARCHITECTURE.md](ARCHITECTURE.md) for the detailed runtime view, module map, and projection flow.

## Contents
1. [Quick Start](#1-quick-start)
2. [Module layout](#2-module-layout)
3. [What stayed from the original module](#3-what-stayed-from-the-original-module)
4. [Runtime flow](#4-runtime-flow)
5. [Local runtime](#5-local-runtime)
6. [Testing split](#6-testing-split)

## 1. Quick Start
<sub>[Back to top](#simple-event-system-ses)</sub>

### 1.1 Start PostgreSQL

Start the local SES database:

```bash
docker compose -f ses/compose.yaml up -d
```

Stop it with:

```bash
docker compose -f ses/compose.yaml down
```

If you want a clean reset of the persisted Docker volume:

```bash
docker compose -f ses/compose.yaml down -v
```

Local database settings:

| Service | URL / Port | Notes |
|---|---|---|
| PostgreSQL | `localhost:5434` | database/user/password are all `ses` |

### 1.2 Start the application

Run the Spring Boot app from the repo root:

```bash
mvn -f ses/app/pom.xml spring-boot:run
```

On startup SES connects to PostgreSQL, validates the schema, and applies the Liquibase changelog.

### 1.3 Open the local docs

Once the app is running, these endpoints are available:

- Swagger UI
  `http://localhost:8053/swagger-ui.html`
- OpenAPI
  `http://localhost:8053/api-docs`

### 1.4 Create and inspect a work request

Create a request:

```bash
curl -X POST http://localhost:8053/api/v1/work-requests \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Ship projection docs",
    "priority": "CRITICAL",
    "budgetCode": "PLATFORM-2026",
    "estimate": 40,
    "requestedBy": "Nikita",
    "correlationId": "quick-start-create"
  }'
```

List the projected requests:

```bash
curl http://localhost:8053/api/v1/work-requests
```

Inspect current budget projections:

```bash
curl http://localhost:8053/api/v1/projections/budgets
```

Watch live projection updates over SSE:

```bash
curl -N http://localhost:8053/api/v1/projections/stream
```

## 2. Module layout
<sub>[Back to top](#simple-event-system-ses)</sub>

`ses` now expands into four modules:

- `ses-events`
  preserved event kernel with workflow events, notifier, subscriptions, and `EventsMonitor`
- `ses-api`
  REST/SSE request and response contracts
- `ses-app`
  Spring Boot service with the event store, command handling, projections, replay, and OpenAPI
- `ses-testing`
  Testcontainers + Cucumber/JUnit end-to-end coverage

`ses-simulator` is no longer the runtime module. Its old random pipeline is retired from the active build.

## 3. What stayed from the original module
<sub>[Back to top](#simple-event-system-ses)</sub>

The strongest original asset was the event kernel, so v1 keeps the same event family:

- `CreatedEvent`
- `AcceptedEvent`
- `RejectedEvent`
- `RunningEvent`
- `CompletedEvent`

It also preserves:

- `EventNotifier`
- `EventsSubscriptionsManager`
- `EventsMonitor`

The main extension is that events now carry event-sourcing metadata such as `eventId`, `occurredAt`,
`actor`, `correlationId`, and `streamVersion`.

## 4. Runtime flow
<sub>[Back to top](#simple-event-system-ses)</sub>

At runtime the service behaves like this:

1. a REST command appends a new workflow event to PostgreSQL `event_store`
2. PostgreSQL `LISTEN/NOTIFY` wakes the projector
3. the projector catches up from its last applied event id
4. read-side SQL projections are updated in order
5. `EventsMonitor` is refreshed from the applied event
6. SSE subscribers receive the projection update

The domain is framed as work requests with:

- title
- priority
- budget code
- estimate
- requested by / acted by

Budget is reserved only when an approval produces `AcceptedEvent`.
Budget-denied approval produces a persisted `RejectedEvent` instead of an empty failure.

## 5. Local runtime
<sub>[Back to top](#simple-event-system-ses)</sub>

Local infrastructure lives in [compose.yaml](compose.yaml):

- PostgreSQL only

The application itself is API-driven:

- no random task generation
- no in-memory finance state
- no simulator worker threads in production runtime

## 6. Testing split
<sub>[Back to top](#simple-event-system-ses)</sub>

Testing is split by responsibility:

- `ses-events`
  unit coverage for event metadata, notifier fan-out, identity, and monitor behavior
- `ses-app`
  unit tests for command rules plus integration tests for event storage, projections, replay, and admin rebuild
- `ses-testing`
  end-to-end HTTP/SSE verification with Testcontainers PostgreSQL
