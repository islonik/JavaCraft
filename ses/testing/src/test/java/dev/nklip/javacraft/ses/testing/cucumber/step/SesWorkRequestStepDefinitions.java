package dev.nklip.javacraft.ses.testing.cucumber.step;

import dev.nklip.javacraft.ses.api.command.ApproveWorkRequest;
import dev.nklip.javacraft.ses.api.command.CompleteWorkRequest;
import dev.nklip.javacraft.ses.api.command.CreateWorkRequest;
import dev.nklip.javacraft.ses.api.command.RejectWorkRequest;
import dev.nklip.javacraft.ses.api.command.StartWorkRequest;
import dev.nklip.javacraft.ses.api.query.BudgetProjectionResponse;
import dev.nklip.javacraft.ses.api.query.RebuildProjectionsResponse;
import dev.nklip.javacraft.ses.api.query.WorkRequestResponse;
import dev.nklip.javacraft.ses.api.query.WorkRequestTimelineEventResponse;
import dev.nklip.javacraft.ses.api.shared.ErrorResponse;
import dev.nklip.javacraft.ses.events.EventStatus;
import dev.nklip.javacraft.ses.events.EventsMonitor;
import dev.nklip.javacraft.ses.events.Priority;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

public class SesWorkRequestStepDefinitions {

    private static final Duration PROJECTION_TIMEOUT = Duration.ofSeconds(10);
    private static final long POLL_INTERVAL_MILLIS = 100L;

    private final TestRestTemplate restTemplate;
    private final int port;
    private final JdbcTemplate jdbcTemplate;
    private final EventsMonitor eventsMonitor;

    private final Map<String, Integer> requestIds = new HashMap<>();

    private ResponseEntity<WorkRequestResponse> lastCommandResponse;
    private ResponseEntity<ErrorResponse> lastErrorResponse;
    private ResponseEntity<RebuildProjectionsResponse> lastRebuildResponse;
    private int requestCounter;

    @Autowired
    public SesWorkRequestStepDefinitions(
            TestRestTemplate restTemplate,
            @LocalServerPort int port,
            JdbcTemplate jdbcTemplate,
            EventsMonitor eventsMonitor
    ) {
        this.restTemplate = restTemplate;
        this.port = port;
        this.jdbcTemplate = jdbcTemplate;
        this.eventsMonitor = eventsMonitor;
    }

    @Before
    public void resetState() {
        jdbcTemplate.execute("truncate table work_request_projection, event_store restart identity cascade");
        jdbcTemplate.execute("alter sequence work_request_id_seq restart with 1000");
        jdbcTemplate.execute("update projection_checkpoint set last_event_store_id = 0 where projection_name = 'ses-projector'");
        jdbcTemplate.execute("delete from budget_projection");
        jdbcTemplate.execute("""
                insert into budget_projection (budget_code, initial_budget, reserved_amount, remaining_budget, last_updated_at)
                select budget_code, initial_budget, 0, initial_budget, current_timestamp
                from budget_reference
                order by budget_code
                """);
        eventsMonitor.clear();
        requestIds.clear();
        lastCommandResponse = null;
        lastErrorResponse = null;
        lastRebuildResponse = null;
        requestCounter = 0;
    }

    @When("a work request is created with title {string}, priority {string}, budget code {string}, estimate {int}, requested by {string}")
    public void createWorkRequest(String title, String priority, String budgetCode, int estimate, String requestedBy) {
        lastCommandResponse = exchange(
                "/api/v1/work-requests",
                HttpMethod.POST,
                new CreateWorkRequest(title, Priority.valueOf(priority), budgetCode, estimate, requestedBy,
                        "corr-create-" + (++requestCounter)),
                WorkRequestResponse.class
        );
        lastErrorResponse = null;

        Assertions.assertEquals(HttpStatus.CREATED, lastCommandResponse.getStatusCode());
        Assertions.assertNotNull(lastCommandResponse.getBody());
        requestIds.put("last", lastCommandResponse.getBody().requestId());
        requestIds.put("request-" + requestCounter, lastCommandResponse.getBody().requestId());
    }

    @When("work request {string} is approved by {string}")
    public void approveWorkRequest(String alias, String actor) {
        lastCommandResponse = exchange(
                "/api/v1/work-requests/%s/approve".formatted(requestId(alias)),
                HttpMethod.POST,
                new ApproveWorkRequest(actor, "corr-approve-" + requestId(alias)),
                WorkRequestResponse.class
        );
        lastErrorResponse = null;
        Assertions.assertEquals(HttpStatus.OK, lastCommandResponse.getStatusCode());
    }

    @When("work request {string} is rejected by {string} with reason {string}")
    public void rejectWorkRequest(String alias, String actor, String reason) {
        lastCommandResponse = exchange(
                "/api/v1/work-requests/%s/reject".formatted(requestId(alias)),
                HttpMethod.POST,
                new RejectWorkRequest(actor, reason, "corr-reject-" + requestId(alias)),
                WorkRequestResponse.class
        );
        lastErrorResponse = null;
        Assertions.assertEquals(HttpStatus.OK, lastCommandResponse.getStatusCode());
    }

    @When("work request {string} is started by {string}")
    public void startWorkRequest(String alias, String actor) {
        lastCommandResponse = exchange(
                "/api/v1/work-requests/%s/start".formatted(requestId(alias)),
                HttpMethod.POST,
                new StartWorkRequest(actor, "corr-start-" + requestId(alias)),
                WorkRequestResponse.class
        );
        lastErrorResponse = null;
        Assertions.assertEquals(HttpStatus.OK, lastCommandResponse.getStatusCode());
    }

    @When("work request {string} is completed by {string}")
    public void completeWorkRequest(String alias, String actor) {
        lastCommandResponse = exchange(
                "/api/v1/work-requests/%s/complete".formatted(requestId(alias)),
                HttpMethod.POST,
                new CompleteWorkRequest(actor, "corr-complete-" + requestId(alias)),
                WorkRequestResponse.class
        );
        lastErrorResponse = null;
        Assertions.assertEquals(HttpStatus.OK, lastCommandResponse.getStatusCode());
    }

    @When("starting work request {string} by {string} fails with conflict")
    public void startWorkRequestFailsWithConflict(String alias, String actor) {
        lastErrorResponse = exchange(
                "/api/v1/work-requests/%s/start".formatted(requestId(alias)),
                HttpMethod.POST,
                new StartWorkRequest(actor, "corr-start-fail-" + requestId(alias)),
                ErrorResponse.class
        );
        lastCommandResponse = null;
        Assertions.assertEquals(HttpStatus.CONFLICT, lastErrorResponse.getStatusCode());
    }

    @When("projections are rebuilt")
    public void rebuildProjections() {
        lastRebuildResponse = exchange("/api/v1/admin/projections/rebuild", HttpMethod.POST, null,
                RebuildProjectionsResponse.class);
        Assertions.assertEquals(HttpStatus.OK, lastRebuildResponse.getStatusCode());
        Assertions.assertNotNull(lastRebuildResponse.getBody());
    }

    @Then("the last command response has HTTP {int} and status {string}")
    public void assertLastCommandResponse(int httpStatus, String status) {
        Assertions.assertNotNull(lastCommandResponse);
        Assertions.assertNotNull(lastCommandResponse.getBody());
        Assertions.assertEquals(HttpStatus.valueOf(httpStatus), lastCommandResponse.getStatusCode());
        Assertions.assertEquals(EventStatus.valueOf(status), lastCommandResponse.getBody().status());
    }

    @Then("the last error response has HTTP {int}")
    public void assertLastErrorResponse(int httpStatus) {
        Assertions.assertNotNull(lastErrorResponse);
        Assertions.assertEquals(HttpStatus.valueOf(httpStatus), lastErrorResponse.getStatusCode());
    }

    @Then("projected work request {string} eventually has status {string}")
    public void projectedWorkRequestEventuallyHasStatus(String alias, String status) {
        EventStatus expectedStatus = EventStatus.valueOf(status);
        WorkRequestResponse projected = await(() -> {
            ResponseEntity<WorkRequestResponse> response = exchange(
                    "/api/v1/work-requests/%s".formatted(requestId(alias)),
                    HttpMethod.GET,
                    null,
                    WorkRequestResponse.class
            );
            return response.getStatusCode() == HttpStatus.OK && response.getBody() != null
                    && response.getBody().status() == expectedStatus ? response.getBody() : null;
        }, "Projected work request %s to reach status %s".formatted(alias, expectedStatus));

        Assertions.assertNotNull(projected);
        Assertions.assertEquals(expectedStatus, projected.status());
    }

    @Then("budget projection {string} eventually has reserved {int} and remaining {int}")
    public void budgetProjectionEventuallyMatches(String budgetCode, int reserved, int remaining) {
        BudgetProjectionResponse projection = await(() -> getBudgetProjection(budgetCode)
                .filter(value -> value.reservedAmount() == reserved && value.remainingBudget() == remaining)
                .orElse(null), "Budget projection %s to become reserved=%s remaining=%s"
                .formatted(budgetCode, reserved, remaining));

        Assertions.assertNotNull(projection);

        Assertions.assertAll(
                () -> Assertions.assertEquals(reserved, projection.reservedAmount()),
                () -> Assertions.assertEquals(remaining, projection.remainingBudget())
        );
    }

    @Then("work request {string} timeline eventually equals statuses {string}")
    public void timelineEventuallyEqualsStatuses(String alias, String csvStatuses) {
        List<EventStatus> expectedStatuses = Arrays.stream(csvStatuses.split(","))
                .map(String::trim)
                .map(EventStatus::valueOf)
                .toList();

        List<WorkRequestTimelineEventResponse> timeline = await(() -> {
            ResponseEntity<List<WorkRequestTimelineEventResponse>> response = exchangeForList(
                    "/api/v1/work-requests/%s/timeline".formatted(requestId(alias)),
                    new ParameterizedTypeReference<>() {
                    }
            );
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                return null;
            }

            List<EventStatus> actualStatuses = response.getBody().stream()
                    .map(WorkRequestTimelineEventResponse::status)
                    .toList();
            return actualStatuses.equals(expectedStatuses) ? response.getBody() : null;
        }, "Timeline %s to equal %s".formatted(alias, expectedStatuses));

        Assertions.assertNotNull(timeline);
        Assertions.assertEquals(expectedStatuses,
                timeline.stream().map(WorkRequestTimelineEventResponse::status).toList());
    }

    @Then("rebuild response reports {int} events, {int} requests, and {int} budgets")
    public void rebuildResponseReports(int events, int requests, int budgets) {
        Assertions.assertNotNull(lastRebuildResponse);
        Assertions.assertNotNull(lastRebuildResponse.getBody());
        Assertions.assertAll(
                () -> Assertions.assertEquals(events, lastRebuildResponse.getBody().eventsReplayed()),
                () -> Assertions.assertEquals(requests, lastRebuildResponse.getBody().requestsProjected()),
                () -> Assertions.assertEquals(budgets, lastRebuildResponse.getBody().budgetsProjected())
        );
    }

    @Then("projected work request list eventually contains {int} items")
    public void projectedWorkRequestListEventuallyContainsItems(int size) {
        List<WorkRequestResponse> requests = await(() -> {
            ResponseEntity<List<WorkRequestResponse>> response = exchangeForList(
                    "/api/v1/work-requests",
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getStatusCode() == HttpStatus.OK && response.getBody() != null
                    && response.getBody().size() == size ? response.getBody() : null;
        }, "Projected work request list to contain %s items".formatted(size));

        Assertions.assertNotNull(requests);
        Assertions.assertEquals(size, requests.size());
    }

    private int requestId(String alias) {
        Integer requestId = requestIds.get(alias);
        Assertions.assertNotNull(requestId, "Unknown request alias: " + alias);
        return requestId;
    }

    private java.util.Optional<BudgetProjectionResponse> getBudgetProjection(String budgetCode) {
        ResponseEntity<List<BudgetProjectionResponse>> response = exchangeForList(
                "/api/v1/projections/budgets",
                new ParameterizedTypeReference<>() {
                }
        );
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return java.util.Optional.empty();
        }
        return response.getBody().stream()
                .filter(projection -> projection.budgetCode().equals(budgetCode))
                .findFirst();
    }

    private <T> ResponseEntity<T> exchange(String path, HttpMethod method, Object body, Class<T> responseType) {
        return restTemplate.exchange(baseUrl() + path, method, body == null ? null : new HttpEntity<>(body), responseType);
    }

    private <T> ResponseEntity<List<T>> exchangeForList(String path, ParameterizedTypeReference<List<T>> responseType) {
        return restTemplate.exchange(baseUrl() + path, HttpMethod.GET, null, responseType);
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private <T> T await(AwaitCondition<T> supplier, String description) {
        long deadline = System.currentTimeMillis() + PROJECTION_TIMEOUT.toMillis();
        while (System.currentTimeMillis() <= deadline) {
            try {
                T value = supplier.get();
                if (value != null) {
                    return value;
                }
            } catch (Exception ignored) {
                // keep polling until the projection catches up
            }
            sleepBeforeRetry();
        }
        Assertions.fail("Timed out waiting for " + description);
        throw new IllegalStateException("Unreachable after failing to await " + description);
    }

    @FunctionalInterface
    private interface AwaitCondition<T> {
        @Nullable T get() throws Exception;
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(POLL_INTERVAL_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for SES projections", e);
        }
    }
}
