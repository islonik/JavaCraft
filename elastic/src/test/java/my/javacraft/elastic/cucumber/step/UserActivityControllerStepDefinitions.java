package my.javacraft.elastic.cucumber.step;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.elastic.cucumber.conf.CucumberSpringConfiguration;
import my.javacraft.elastic.model.UserClick;
import my.javacraft.elastic.model.UserClickResponse;
import my.javacraft.elastic.model.UserActivity;
import my.javacraft.elastic.service.activity.UserActivityService;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;
@Slf4j
@Scope(SCOPE_CUCUMBER_GLUE)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class UserActivityControllerStepDefinitions {

    @LocalServerPort
    int port;

    @Autowired
    ElasticsearchClient esClient;

    @Given("user {string} doesn't have any events")
    public void clearUserHistory(String userId) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest.Builder()
                .index(UserActivityService.INDEX_USER_HISTORY)
                .query(q -> q.term(t -> t
                        .field(UserActivityService.USER_ID)
                        .value(v -> v.stringValue(userId))
                )).build();
        DeleteByQueryResponse deleteByQueryResponse = esClient.deleteByQuery(deleteByQueryRequest);
        Assertions.assertNotNull(deleteByQueryResponse);
        log.info("All events for user '{}' are deleted!", userId);
    }

    @When("add new event with expected result = {string}")
    public void addNewEvent(String expectedResult, DataTable dataTable) throws JsonProcessingException {
        String jsonBody = jsonBody(dataTable);

        log.info("created json:\n" + jsonBody);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<UserClickResponse> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/services/user-activity".formatted(port),
                HttpMethod.POST,
                entity,
                UserClickResponse.class
        );
        Assertions.assertNotNull(httpResponse);
        Assertions.assertNotNull(httpResponse.getBody());
        Assertions.assertEquals(expectedResult, httpResponse.getBody().getResult().toString());

        ObjectMapper objectMapper = new ObjectMapper();
        log.info("{}", objectMapper.writeValueAsString(httpResponse.getBody()));
    }

    @When("there are {int} requests")
    public void sendMultipleRequestInParallel(Integer requests, DataTable dataTable) throws Exception {
        log.info("sending {} requests in parallel...", requests);

        List<Future<HttpEntity<UserClickResponse>>> futureClicks = new ArrayList<>();
        try (ExecutorService executorService = Executors.newFixedThreadPool(requests)) {
            for (int i = 0; i < requests; i++) {
                Future<HttpEntity<UserClickResponse>> callbackResult = executorService.submit(() -> {
                    String jsonBody = jsonBody(dataTable);

                    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

                    HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

                    RestTemplate restTemplate = new RestTemplate();

                    return restTemplate.exchange(
                            "http://localhost:%s/api/services/user-activity".formatted(port),
                            HttpMethod.POST,
                            entity,
                            UserClickResponse.class
                    );
                });
                futureClicks.add(callbackResult);
            }
        }

        Assertions.assertFalse(futureClicks.isEmpty());

        for (Future<HttpEntity<UserClickResponse>> futureUserClickResponse : futureClicks) {
            HttpEntity<UserClickResponse> userClickResponseHttpEntity = futureUserClickResponse.get();
            UserClickResponse userClickResponse = userClickResponseHttpEntity.getBody();
            Assertions.assertNotNull(userClickResponse);
            log.info(userClickResponse.toString());
        }
    }

    @Then("user {string} has {int} hit counts for documentId = {string}, searchType = {string} and pattern = {string}")
    public void checkHitCounts(
            String userId,
            int hitCounts,
            String recordId,
            String type,
            String pattern) throws InterruptedException {
        CucumberSpringConfiguration.waitAsElasticSearchIsEventuallyConsistentDB();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<List<UserActivity>> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/services/user-activity/users/%s".formatted(port, userId),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        Assertions.assertNotNull(httpResponse);
        Assertions.assertNotNull(httpResponse.getBody());
        Assertions.assertEquals(1, httpResponse.getBody().size());
        UserActivity userActivity = httpResponse.getBody().getFirst();
        Assertions.assertEquals(hitCounts, userActivity.getCount());
        Assertions.assertEquals("%s-%s-%s".formatted(recordId, type, userId), userActivity.getElasticId());
        Assertions.assertEquals(recordId, userActivity.getRecordId());
        Assertions.assertEquals(pattern, userActivity.getSearchValue());

    }

    @Then("user {string} has next sorting results")
    public void testSortingOrder(String userId, DataTable dataTable) throws InterruptedException {
        CucumberSpringConfiguration.waitAsElasticSearchIsEventuallyConsistentDB();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<List<UserActivity>> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/services/user-activity/users/%s".formatted(port, userId),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        Assertions.assertNotNull(httpResponse);
        Assertions.assertNotNull(httpResponse.getBody());
        Assertions.assertEquals(2, httpResponse.getBody().size());
        List<List<String>> expectedResults = dataTable.cells();
        for (int i = 0; i < httpResponse.getBody().size(); i++) {
            UserActivity userActivity = httpResponse.getBody().get(i);
            Assertions.assertEquals(expectedResults.get(i).get(0), userActivity.getSearchValue());
            Assertions.assertEquals(Long.parseLong(expectedResults.get(i).get(1)), userActivity.getCount());
        }
    }

    private String jsonBody(DataTable dataTable) throws JsonProcessingException {
        UserClick userClick = new UserClick();
        List<String> data = dataTable.cells().getFirst();
        userClick.setUserId(data.get(0));
        userClick.setRecordId(data.get(1));
        userClick.setSearchType(data.get(2));
        userClick.setSearchPattern(data.get(3));

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(userClick);
    }

}
