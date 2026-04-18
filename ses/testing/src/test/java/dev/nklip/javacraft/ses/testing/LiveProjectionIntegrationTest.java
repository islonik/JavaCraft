package dev.nklip.javacraft.ses.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nklip.javacraft.ses.api.command.CreateWorkRequest;
import dev.nklip.javacraft.ses.api.query.WorkRequestResponse;
import dev.nklip.javacraft.ses.events.EventStatus;
import dev.nklip.javacraft.ses.events.Priority;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class LiveProjectionIntegrationTest extends AbstractSesTestingIntegrationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    @Test
    void listenNotifyEventuallyProjectsCreatedWorkRequest() {
        ResponseEntity<WorkRequestResponse> createResponse = restTemplate.postForEntity(
                baseUrl() + "/api/v1/work-requests",
                new CreateWorkRequest("Wake up projector", Priority.CRITICAL, "PLATFORM-2026", 25,
                        "Nikita", "corr-live"),
                WorkRequestResponse.class
        );

        Assertions.assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Assertions.assertNotNull(createResponse.getBody());

        WorkRequestResponse projected = awaitProjectedRequest(createResponse.getBody().requestId());

        Assertions.assertNotNull(projected);
        Assertions.assertEquals(EventStatus.CREATED, projected.status());
    }

    @Test
    void sseSubscribersReceiveLiveProjectionUpdates() throws Exception {
        CompletableFuture<String> firstDataLine = new CompletableFuture<>();
        HttpURLConnection connection = (HttpURLConnection) URI.create(baseUrl() + "/api/v1/projections/stream")
                .toURL()
                .openConnection();
        connection.setRequestProperty("Accept", "text/event-stream");
        connection.setReadTimeout((int) TIMEOUT.toMillis());
        connection.connect();

        Thread readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        firstDataLine.complete(line.substring("data:".length()).trim());
                        break;
                    }
                }
            } catch (Exception e) {
                firstDataLine.completeExceptionally(e);
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();

        try {
            ResponseEntity<WorkRequestResponse> createResponse = restTemplate.postForEntity(
                    baseUrl() + "/api/v1/work-requests",
                    new CreateWorkRequest("Stream me", Priority.NORMAL, "OPS-2026", 10,
                            "Nikita", "corr-sse"),
                    WorkRequestResponse.class
            );

            Assertions.assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
            Assertions.assertNotNull(createResponse.getBody());

            String payload = firstDataLine.get(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            Assertions.assertAll(
                    () -> Assertions.assertTrue(payload.contains("\"updateType\":\"WORK_REQUEST_UPDATED\"")),
                    () -> Assertions.assertTrue(payload.contains("\"requestId\":" + createResponse.getBody().requestId())),
                    () -> Assertions.assertTrue(payload.contains("\"status\":\"CREATED\""))
            );
        } finally {
            connection.disconnect();
        }
    }

    private WorkRequestResponse awaitProjectedRequest(int requestId) {
        long deadline = System.currentTimeMillis() + TIMEOUT.toMillis();
        while (System.currentTimeMillis() <= deadline) {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl() + "/api/v1/work-requests/" + requestId,
                    HttpMethod.GET,
                    null,
                    String.class
            );
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                try {
                    return objectMapper.readValue(response.getBody(), WorkRequestResponse.class);
                } catch (Exception e) {
                    throw new IllegalStateException("Unable to deserialize projected SES work request", e);
                }
            }
            sleepBeforeRetry();
        }
        Assertions.fail("Projected work request %s did not become visible in time".formatted(requestId));
        return null;
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for live projection", e);
        }
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
