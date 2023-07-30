package my.javacraft.soap2rest.rest.app.cucumber.step;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.dao.ElectricMetricDao;
import my.javacraft.soap2rest.rest.app.dao.entity.ElectricMetric;
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
public class ElectricDefinition {

    @LocalServerPort
    int port;

    @Autowired
    private ElectricMetricDao electricMetricDao;

    @Given("the account {string} doesn't have electric metrics")
    public void cleanElectricMetrics(String account) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set(AuthenticationService.AUTH_TOKEN_HEADER_NAME, "57AkjqNuz44QmUHQuvVo");

        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<Boolean> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/v1/smart/%s/electric".formatted(port, account),
                HttpMethod.DELETE,
                entity,
                Boolean.class
        );

        Assertions.assertNotNull(httpResponse);
        Assertions.assertNotNull(httpResponse.getBody());
    }
    @When("an account {string} submits a PUT request with a new electric reading: {string}, {string}, {string}")
    public void applyPutRequestWithElectricReading(
            String account, String meterId, String reading, String date) {
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
                "http://localhost:%s/api/v1/smart/%s/electric".formatted(port, account),
                HttpMethod.PUT,
                entity,
                ElectricMetric.class
        );

        Assertions.assertNotNull(httpResponse);
        Assertions.assertNotNull(httpResponse.getBody());
    }

    @Then("check the latest electric reading for the meterId = {string} is equal = {string}")
    public void checkLatestElectricReading(String meterId, String reading) {
        Metric latestMetric = electricMetricDao.findTopByMeterIdInOrderByDateDesc(
                Collections.singletonList(Long.parseLong(meterId))
        ).toApiMetric();
        Assertions.assertEquals(0, latestMetric
                .getReading()
                .compareTo(new BigDecimal(reading))
        );
    }

    @Then("check there is no electric readings for the meterId = {string}")
    public void checkNoElectricMetric(String meterId) {
        List<Metric> metrics = electricMetricDao.findByMeterIds(
                Collections.singletonList(Long.parseLong(meterId))
        ).stream().map(ElectricMetric::toApiMetric).toList();
        Assertions.assertEquals(0, metrics.size());
    }

}
