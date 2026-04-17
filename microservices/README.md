# Microservices
<sub>[Back to JavaCraft](../README.md#micro-java-samples)</sub>

Full-stack Spring Boot services, each with its own persistence layer, Docker infrastructure, and API surface.

## Modules

- [ess](ess/README.md) - Elasticsearch-backed content search and Reddit-style post voting with ranked feeds
- [linker](linker/README.md) - URL shortening service with collision-safe IDs, redirects, and analytics backed by MongoDB
- [openflights](openflights/README.md) - OpenFlights dataset ingestion pipeline via Kafka into PostgreSQL with admin HTTP endpoints
- [soap2rest](soap2rest/README.md) - Strangler-style SOAP-to-REST migration with synchronous and async JMS flows
