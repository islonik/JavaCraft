package my.javacraft.soap2rest.rest.app.cucumber.step;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.dao.ElectricMetricDao;
import my.javacraft.soap2rest.rest.app.dao.entity.ElectricMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.MetricEntity;
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

/**
 * This test shows how to create HTTP requests <b>without</b> <i>io.rest-assured:rest-assured</i> library.
 */
@Scope(SCOPE_CUCUMBER_GLUE)
@RequiredArgsConstructor
public class ElectricDefinition {

    @LocalServerPort
    int port;

    private final ElectricMetricDao electricMetricDao;

    private HttpEntity<String> prepareHttpEntity() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(AuthenticationService.AUTH_TOKEN_HEADER_NAME, "57AkjqNuz44QmUHQuvVo");
        return new HttpEntity<>(null, headers);
    }

    @Given("the account {long} doesn't have electric metrics")
    public void cleanElectricMetrics(Long accountId) {
        HttpEntity<String> entity = prepareHttpEntity();
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<Boolean> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/v1/smart/%s/electric".formatted(port, accountId),
                HttpMethod.DELETE,
                entity,
                Boolean.class
        );

        Assertions.assertNotNull(httpResponse);
        Assertions.assertEquals(true, httpResponse.getBody());
    }
    @When("an account {long} submits a PUT request with a new electric reading: {long}, {bigdecimal}, {string}")
    public void applyPutRequestWithElectricReading(
            Long accountId, Long meterId, BigDecimal reading, String date) {
        String jsonBody = """
        {
            "meterId": %s,
            "reading":"%s",
            "date": "%s"
        }
        """.formatted(meterId, reading, date);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(AuthenticationService.AUTH_TOKEN_HEADER_NAME, "57AkjqNuz44QmUHQuvVo");

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<ElectricMetric> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/v1/smart/%s/electric".formatted(port, accountId),
                HttpMethod.PUT,
                entity,
                ElectricMetric.class
        );

        Assertions.assertNotNull(httpResponse);
        Assertions.assertNotNull(httpResponse.getBody());
        Assertions.assertEquals(meterId, httpResponse.getBody().getMeterId());
        Assertions.assertEquals(reading, httpResponse.getBody().getReading());
        Assertions.assertEquals(date, httpResponse.getBody().getDate().toString());
    }

    @Then("check the latest electric reading for the meterId = {long} is equal = {bigdecimal}")
    public void checkLatestElectricReading(Long meterId, BigDecimal reading) {
        Metric latestMetric = electricMetricDao.findTopByMeterIdInOrderByDateDesc(
                Collections.singletonList(meterId)
        ).toApiMetric();
        Assertions.assertEquals(0, latestMetric
                .getReading()
                .compareTo(reading)
        );
    }

    @Then("check the latest electric reading for the account = {long} extra values: {bigdecimal}, {long}, {bigdecimal}")
    public void checkLatestGasReadingForExtraValues(
            Long accountId, BigDecimal usageSince, Long periodSince, BigDecimal avgUsage) {
        HttpEntity<String> entity = prepareHttpEntity();

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<Metric> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/v1/smart/%s/electric/latest".formatted(port, accountId),
                HttpMethod.GET,
                entity,
                Metric.class
        );

        Assertions.assertNotNull(httpResponse);
        Assertions.assertNotNull(httpResponse.getBody());

        Metric response = httpResponse.getBody();
        Assertions.assertEquals(usageSince, response.getUsageSinceLastRead());
        Assertions.assertEquals(periodSince, response.getPeriodSinceLastRead());
        Assertions.assertEquals(avgUsage, response.getAvgDailyUsage());
    }

    @Then("check there is no electric readings for the meterId = {long}")
    public void checkNoElectricMetric(Long meterId) {
        List<Metric> metrics = electricMetricDao.findByMeterIds(
                Collections.singletonList(meterId)
        ).stream().map(MetricEntity::toApiMetric).toList();
        Assertions.assertEquals(0, metrics.size());
    }

}
