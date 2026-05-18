package dev.nklip.javacraft.soap2rest.soap.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class HttpCallServicePerformanceTest {

    private static final String AUTH_TOKEN = "57AkjqNuz44QmUHQuvVo";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);
    private static final int WARMUP_REQUESTS = 10;
    private static final int MEASURED_REQUESTS = 500;
    private static final Duration MAX_AVERAGE_LATENCY = Duration.ofMillis(100);

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void testRepeatedGetShouldReuseConnectionsAndStayResponsive() throws Exception {
        Set<Integer> remotePorts = new HashSet<>();
        AtomicInteger requestCount = new AtomicInteger();
        startServer("/api/v1/smart/1", exchange -> {
            requestCount.incrementAndGet();
            remotePorts.add(exchange.getRemoteAddress().getPort());
            writeResponse(exchange, HttpStatus.OK.value(), MediaType.TEXT_PLAIN_VALUE, "ok");
        });

        HttpCallService service = new HttpCallService(
                "http://localhost",
                Integer.toString(server.getAddress().getPort()),
                AUTH_TOKEN,
                CONNECT_TIMEOUT,
                READ_TIMEOUT
        );

        Duration elapsed = Duration.ZERO;
        Duration averageLatency = Duration.ZERO;
        try {
            for (int i = 0; i < WARMUP_REQUESTS; i++) {
                ResponseEntity<String> response = service.get("/api/v1/smart/1", String.class);
                Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
                Assertions.assertEquals("ok", response.getBody());
            }

            long startedAt = System.nanoTime();
            for (int i = 0; i < MEASURED_REQUESTS; i++) {
                ResponseEntity<String> response = service.get("/api/v1/smart/1", String.class);
                Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
                Assertions.assertEquals("ok", response.getBody());
            }
            elapsed = Duration.ofNanos(System.nanoTime() - startedAt);
            averageLatency = elapsed.dividedBy(MEASURED_REQUESTS);

            Assertions.assertEquals(
                    WARMUP_REQUESTS + MEASURED_REQUESTS,
                    requestCount.get(),
                    "Unexpected number of local HTTP calls recorded"
            );
            Assertions.assertTrue(
                    remotePorts.size() <= 2,
                    "Expected the JDK HttpClient to reuse the local connection, but saw %s TCP connections"
                            .formatted(remotePorts.size())
            );
            Assertions.assertTrue(
                    averageLatency.compareTo(MAX_AVERAGE_LATENCY) < 0,
                    "Expected average latency below %s but was %s across %s measured requests"
                            .formatted(MAX_AVERAGE_LATENCY, averageLatency, MEASURED_REQUESTS)
            );
        } finally {
            System.out.println("""
                    HttpCallServicePerformanceTest summary:
                    - warmupRequests: %s
                    - measuredRequests: %s
                    - totalRequestsCaptured: %s
                    - uniqueTcpConnections: %s
                    - remotePorts: %s
                    - totalMeasuredElapsed: %s
                    - averageLatencyPerRequest: %s
                    - maxAllowedAverageLatency: %s
                    """.formatted(
                    WARMUP_REQUESTS,
                    MEASURED_REQUESTS,
                    requestCount.get(),
                    remotePorts.size(),
                    new TreeSet<>(remotePorts),
                    elapsed,
                    averageLatency,
                    MAX_AVERAGE_LATENCY
            ));
        }
    }

    private void startServer(String path, ExchangeHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext(path, exchange -> {
            try (exchange) {
                handler.handle(exchange);
            }
        });
        server.start();
    }

    private void writeResponse(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    @FunctionalInterface
    private interface ExchangeHandler {
        void handle(HttpExchange exchange) throws IOException;
    }
}
