package my.javacraft.soap2rest.rest.app.cucumber.step;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import my.javacraft.soap2rest.rest.app.dao.GasMetricDao;
import my.javacraft.soap2rest.rest.app.dao.entity.GasMetric;
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

    @Given("the account {string} doesn't have gas metrics")
    public void cleanGasMetrics(String account) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<Boolean> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/v1/smart/%s/gas".formatted(port, account),
                HttpMethod.DELETE,
                entity,
                Boolean.class
        );

        Assertions.assertNotNull(httpResponse);
        Assertions.assertNotNull(httpResponse.getBody());
    }

    @When("the account {string} submits a PUT request with a new gas reading: {string}, {string}, {string}")
    public void applyPutRequestWithGasReading(
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

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<GasMetric> httpResponse = restTemplate.exchange(
                "http://localhost:%s/api/v1/smart/%s/gas".formatted(port, account),
                HttpMethod.PUT,
                entity,
                GasMetric.class
        );

        Assertions.assertNotNull(httpResponse);
        Assertions.assertNotNull(httpResponse.getBody());
    }

    @Then("check the latest gas reading for the meterId = {string} is equal = {string}")
    public void checkLatestGasReading(String meterId, String reading) {
        GasMetric latestMetric = gasMetricDao.findTopByMeterIdInOrderByDateDesc(
                Collections.singletonList(Long.parseLong(meterId))
        );
        Assertions.assertEquals(0, latestMetric
                .getReading()
                .compareTo(new BigDecimal(reading))
        );
    }

    @Then("check there is no gas readings for the meterId = {string}")
    public void checkNoGasMetric(String meterId) {
        List<GasMetric> metrics = gasMetricDao.findByMeterIds(
                Collections.singletonList(Long.parseLong(meterId))
        );
        Assertions.assertEquals(0, metrics.size());
    }
}
