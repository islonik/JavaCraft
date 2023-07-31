package my.javacraft.soap2rest.rest.app.cucumber.step;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.dao.GasMetricDao;
import my.javacraft.soap2rest.rest.app.dao.entity.GasMetric;
import my.javacraft.soap2rest.rest.app.security.AuthenticationService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
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

@Scope(SCOPE_CUCUMBER_GLUE)
public class GasDefinition {

    @LocalServerPort
    int port;

    @Autowired
    private GasMetricDao gasMetricDao;

    @Given("the account {long} doesn't have gas metrics")
    public void cleanGasMetrics(Long accountId) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(AuthenticationService.AUTH_TOKEN_HEADER_NAME, "57AkjqNuz44QmUHQuvVo");

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<Boolean> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/smart/reads/%s/gas".formatted(port, accountId),
                HttpMethod.DELETE,
                entity,
                Boolean.class
        );

        Assertions.assertNotNull(httpResponse);
        Assertions.assertNotNull(httpResponse.getBody());
    }

    @When("the account {long} submits a PUT request with a new gas reading: {long}, {bigdecimal}, {string}")
    public void applyPutRequestWithGasReading(
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

        HttpEntity<GasMetric> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/smart/reads/%s/gas".formatted(port, accountId),
                HttpMethod.PUT,
                entity,
                GasMetric.class
        );

        Assertions.assertNotNull(httpResponse);
        Assertions.assertNotNull(httpResponse.getBody());
    }

    @Then("check the latest gas reading for the meterId = {long} is equal = {bigdecimal}")
    public void checkLatestGasReading(Long meterId, BigDecimal reading) {
        Metric latestMetric = gasMetricDao.findTopByMeterIdInOrderByDateDesc(
                Collections.singletonList(meterId)
        ).toApiMetric();
        Assertions.assertEquals(0, latestMetric
                .getReading()
                .compareTo(reading)
        );
    }

    @Then("check the latest gas reading for the account = {long} extra values: {bigdecimal}, {long}, {bigdecimal}")
    public void checkLatestGasReadingForExtraValues(
            Long accountId, BigDecimal usageSince, Long periodSince, BigDecimal avgUsage) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(AuthenticationService.AUTH_TOKEN_HEADER_NAME, "57AkjqNuz44QmUHQuvVo");

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<Metric> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/smart/reads/%s/gas/latest".formatted(port, accountId),
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

    @Then("check there is no gas readings for the meterId = {long}")
    public void checkNoGasMetric(Long meterId) {
        List<Metric> metrics = gasMetricDao.findByMeterIds(
                Collections.singletonList(meterId)
        ).stream().map(GasMetric::toApiMetric).toList();
        Assertions.assertEquals(0, metrics.size());
    }
}
