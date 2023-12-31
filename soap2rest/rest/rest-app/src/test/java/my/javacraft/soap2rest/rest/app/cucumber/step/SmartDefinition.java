package my.javacraft.soap2rest.rest.app.cucumber.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.api.Metrics;
import my.javacraft.soap2rest.rest.app.security.AuthenticationService;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Slf4j
@Scope(SCOPE_CUCUMBER_GLUE)
public class SmartDefinition {

    @LocalServerPort
    int port;

    private RequestSpecification prepareBaseRequest(Long accountId) {
        RequestSpecification request = RestAssured.given();
        request.baseUri("http://localhost:%s/api/v1/smart/%s".formatted(port, accountId));
        request.header(AuthenticationService.AUTH_TOKEN_HEADER_NAME, "57AkjqNuz44QmUHQuvVo");

        return request;
    }

    private HttpEntity<String> prepareHttpEntity(String jsonBody) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(AuthenticationService.AUTH_TOKEN_HEADER_NAME, "57AkjqNuz44QmUHQuvVo");
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return new HttpEntity<>(jsonBody, headers);
    }

    @Given("the account {long} doesn't have any metrics")
    public void cleanGasMetrics(Long accountId) {
        RequestSpecification request = prepareBaseRequest(accountId);

        Response response = request.delete();

        String result = response.asString();
        Assertions.assertEquals("true", result);
    }

    @When("the account {long} submits a PUT request with new metrics")
    public void applyPutRequestWithGasReading(Long accountId, DataTable table) throws Exception {
        Metrics metrics = data2Metrics(accountId, table);

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(metrics);

        log.info(jsonBody);

        HttpEntity<String> entity = prepareHttpEntity(jsonBody);

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpEntity<Boolean> httpResponse = restTemplate.exchange(
                    "http://localhost:%s/api/v1/smart/%s".formatted(port, accountId),
                    HttpMethod.PUT,
                    entity,
                    Boolean.class
            );
            Boolean response = httpResponse.getBody();

            Assertions.assertNotNull(response);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private Metrics data2Metrics(Long accountId, DataTable table) {
        Metrics metrics = new Metrics();
        metrics.setAccountId(accountId);

        List<Metric> gasMetricList = new ArrayList<>();
        List<Metric> electricMetricsList = new ArrayList<>();

        List<List<String>> rows = table.cells();
        for (List<String> row : rows) {
            String type = row.get(0);

            Metric metric = new Metric();
            metric.setMeterId(Long.parseLong(row.get(1)));
            metric.setReading(new BigDecimal(row.get(2)));
            metric.setDate(Date.valueOf(row.get(3)));

            if (type.equalsIgnoreCase("gas")) {
                gasMetricList.add(metric);
            } else if (type.equalsIgnoreCase("ele")) {
                electricMetricsList.add(metric);
            }
        }

        metrics.setGasReadings(gasMetricList);
        metrics.setElecReadings(electricMetricsList);

        return metrics;
    }

}
