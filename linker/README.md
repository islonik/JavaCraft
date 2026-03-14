# Linker
A short-link service that stores long URLs, generates collision-safe short IDs, redirects users, and tracks basic analytics.

Tags: Spring Boot, MongoDb, Swagger/OpenAPI

### Start-up URL
http://localhost:8080/swagger-ui/index.html

## What it now demonstrates

- Collision-safe short ID creation with retry strategy.
- Idempotent link creation by URL (same URL returns the same existing short link).
- Expirable links (`expirationDate`) with `410 Gone` behavior after expiration.
- Redirect analytics (`redirectCount`, `lastAccessDate`).
- MongoDB persistence with repository-level and controller-level integration tests.

## API

Base path: `/api/v1/links`

## Swagger UI

After application startup, open:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

It exposes all `LinkController` endpoints with request/response schemas.

1. Create short link

`PUT /api/v1/links`

Body: raw URL string (or JSON string in Swagger UI).

Response: full short URL, for example:

`http://localhost:8080/api/v1/links/Ab12Cd`

Behavior:
- If URL is new, a new short URL is created and returned.
- If the same URL already exists, the existing short URL is returned and no duplicate entity is created.

2. Redirect by short id

`GET /api/v1/links/{shortUrl}`

- `302 Found` + `Location` header when active.
- `404 Not Found` when unknown.
- `410 Gone` when expired.

3. Analytics by short id

`GET /api/v1/links/{shortUrl}/analytics`

Response example:

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

## Collision-safe ID strategy

- IDs are generated with configurable length.
- Service checks `existsByShortUrl(...)` before insert.
- DB-level unique index on `shortUrl` protects against concurrent races.
- On duplicate-key race, service retries with a new generated id.

Config in `application.yaml`:

```yaml
linker:
  short-url:
    length: 6
    max-attempts: 64
  expiration-days: 30
```

### How to forward URL request to another request

```java
@RestController
@RequestMapping(path = "/api/v1/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkServices linkServices;
    
    // redirection
    @GetMapping(value = "/{shortUrl}")
    public ResponseEntity<byte[]> shortUrl2FullUrl(@PathVariable("shortUrl") String shortUrl) {
        LinkServices.ResolveLinkResult resolveLinkResult = linkServices.resolveLink(shortUrl);
        if (resolveLinkResult.status() == LinkServices.ResolveStatus.NOT_FOUND) {
            return ResponseEntity.notFound().build();
        }
        if (resolveLinkResult.status() == LinkServices.ResolveStatus.EXPIRED) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, resolveLinkResult.url());

        return new ResponseEntity<>(null, headers, HttpStatus.FOUND);
    }
    
}
    
```

## Tests

Run all linker tests:

```bash
mvn -pl linker test
```

Coverage includes:

- Unit tests for controller/service/symbol generation.
- Persistence test with in-memory Mongo (`LinkRepositoryTest`).
- Spring MVC integration test with in-memory Mongo (`LinkControllerIntegrationTest`).
