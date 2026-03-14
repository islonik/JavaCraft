# Linker

A URL shortening service built with Spring Boot and MongoDB. Stores long URLs, generates
collision-safe short IDs, redirects users, and tracks redirect analytics.

**Stack:** Spring Boot, MongoDB, Swagger/OpenAPI

---

## Quick Start

**Prerequisites:** MongoDB running on `localhost:27017`

```bash
mvn -pl linker spring-boot:run
```

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui/index.html | Swagger UI |
| http://localhost:8080/v3/api-docs | OpenAPI JSON |

---

## Architecture

```mermaid
flowchart TD
    Client["HTTP Client"]
    RC["LinkController\n/api/v1/links"]
    SVC["LinkServices"]
    SYM["SymbolGeneratorServices"]
    REPO["LinkRepository\nMongoRepository"]
    DB[("MongoDB\ndatabase: links\ncollection: link")]

    Client -->|REST| RC
    RC --> SVC
    SVC -->|generate ID| SYM
    SVC -->|CRUD| REPO
    REPO --> DB
```

---

## API Reference

Base path: `/api/v1/links`

| Method | Path | Description | Success response |
|--------|------|-------------|------------------|
| `PUT` | `/` | Create or retrieve a short link | `200` plain-text short URL |
| `GET` | `/{shortUrl}` | Redirect to original URL | `302 Found` + `Location` header |
| `GET` | `/{shortUrl}/analytics` | Get redirect analytics | `200` `LinkAnalytics` JSON |
| `GET` | `/` | List all stored links | `200` `List<Link>` JSON |

---

### PUT /api/v1/links — Create Short Link

Request body: raw URL string (plain text or JSON string in Swagger).

```
https://example.org/very/long/path?with=params
```

Response: full short URL as plain text.

```
http://localhost:8080/api/v1/links/Ab12Cd
```

**Idempotent:** submitting the same URL a second time returns the existing short link — no
duplicates are stored in the database.

---

### GET /api/v1/links/{shortUrl} — Redirect

| Condition | Status |
|-----------|--------|
| Link found and not expired | `302 Found` + `Location` header pointing to original URL |
| Unknown short code | `404 Not Found` |
| Link past its expiration date | `410 Gone` |

---

### GET /api/v1/links/{shortUrl}/analytics — Analytics

Returns `404 Not Found` for an unknown short code, otherwise:

```json
{
  "shortUrl": "Ab12Cd",
  "url": "https://example.org/page",
  "creationDate": "2026-03-11T11:12:00.000+00:00",
  "expirationDate": "2026-04-10T11:12:00.000+00:00",
  "redirectCount": 3,
  "lastAccessDate": "2026-03-11T11:14:21.000+00:00",
  "expired": false
}
```

---

## Data Model

**`Link`** — MongoDB document stored in collection `link`.

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `url` | String | Original long URL |
| `shortUrl` | String | Unique short code (unique index) |
| `creationDate` | Date | Creation timestamp |
| `expirationDate` | Date | Expiration timestamp |
| `redirectCount` | long | Number of successful redirects |
| `lastAccessDate` | Date | Timestamp of the most recent redirect |

---

## Collision-safe ID Strategy

Short codes are random alphanumeric strings (default: 6 characters, charset A–Z a–z 0–9).
The service handles both pre-insert collisions and concurrent-insert races:

```mermaid
flowchart TD
    A[Generate random ID] --> B{existsByShortUrl?}
    B -- yes --> C{attempts < maxAttempts?}
    C -- yes --> A
    C -- no --> ERR[throw IllegalStateException]
    B -- no --> INS[INSERT Link document]
    INS --> DKE{DuplicateKeyException?}
    DKE -- same URL inserted concurrently --> RET_EXIST[return existing short URL]
    DKE -- different collision race --> A
    DKE -- no exception --> RET_NEW[return new short URL]
```

- **Pre-insert check:** `existsByShortUrl()` detects known collisions before the write.
- **Unique index on `shortUrl`:** prevents duplicates at the database level.
- **Post-insert guard:** catches `DuplicateKeyException` for the concurrent-insert race:
  - If the same original URL was inserted by another request in parallel → return its short URL.
  - If a different collision happened → retry with a freshly generated ID.

---

## Configuration

`linker/src/main/resources/application.yaml`:

```yaml
host: http://localhost:8080/api/v1/links

linker:
  short-url:
    length: 6          # character length of each generated short code
    max-attempts: 64   # maximum retries before giving up with IllegalStateException
  expiration-days: 30  # days until a link expires (counted from creation)

spring:
  data:
    mongodb:
      database: links
      uri: mongodb://localhost:27017/?directConnection=true/links
```

---

## Tests

Run the full test suite for this module:

```bash
mvn -pl linker test
```

| Test class | Type | Covers |
|-----------|------|--------|
| `SymbolGeneratorServicesTest` | Unit | ID generation: length, charset, determinism, rejection of invalid length |
| `LinkServicesTest` | Unit | Create/resolve business logic, collision retry, URL deduplication, expiration check |
| `LinkControllerTest` | Unit (MockMvc) | HTTP layer: status codes, `Location` header on redirect, response bodies |
| `LinkRepositoryTest` | Persistence | MongoDB queries via in-memory server |
| `LinkControllerIntegrationTest` | Integration | Full flow: create → redirect → analytics; expiration; deduplication across requests |

In-memory MongoDB is provided by `mongo-java-server` — no external MongoDB process is
required for tests.
