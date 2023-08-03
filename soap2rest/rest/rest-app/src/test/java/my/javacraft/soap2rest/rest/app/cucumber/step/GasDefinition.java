package my.javacraft.soap2rest.rest.app.cucumber.step;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import my.javacraft.soap2rest.rest.api.Metric;
import my.javacraft.soap2rest.rest.app.dao.GasMetricDao;
import my.javacraft.soap2rest.rest.app.dao.entity.GasMetric;
import my.javacraft.soap2rest.rest.app.dao.entity.MetricEntity;
import my.javacraft.soap2rest.rest.app.security.AuthenticationService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import io.restassured.RestAssured;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Scope(SCOPE_CUCUMBER_GLUE)
public class GasDefinition {

    @LocalServerPort
    int port;

    @Autowired
    private GasMetricDao gasMetricDao;

    private RequestSpecification prepareBaseRequest(Long accountId) {
        RequestSpecification request = RestAssured.given();
        request.baseUri("http://localhost:%s/api/smart/reads/%s/gas".formatted(port, accountId));
        request.header(AuthenticationService.AUTH_TOKEN_HEADER_NAME, "57AkjqNuz44QmUHQuvVo");

        return request;
    }

    @Given("the account {long} doesn't have gas metrics")
    public void cleanGasMetrics(Long accountId) {
        RequestSpecification request = prepareBaseRequest(accountId);

        Response response = request.delete();

        String result = response.asString();
        Assertions.assertEquals("true", result);
    }

    @When("the account {long} submits a PUT request with a new gas reading: {long}, {bigdecimal}, {string}")
    public void applyPutRequestWithGasReading(
            Long accountId, Long meterId, BigDecimal reading, String date) {
        RequestSpecification request = prepareBaseRequest(accountId);
        request.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String jsonBody = """
        {
            "meterId": %s,
            "reading":"%s",
            "date": "%s"
        }
        """.formatted(meterId, reading, date);

        GasMetric response = request.body(jsonBody).put().as(GasMetric.class);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(meterId, response.getMeterId());
        Assertions.assertEquals(reading, response.getReading());
        Assertions.assertEquals(date, response.getDate().toString());
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
        RequestSpecification request = prepareBaseRequest(accountId);

        Metric response = request.get("/latest").as(Metric.class);

        Assertions.assertEquals(usageSince, response.getUsageSinceLastRead());
        Assertions.assertEquals(periodSince, response.getPeriodSinceLastRead());
        Assertions.assertEquals(avgUsage, response.getAvgDailyUsage());
    }

    @Then("check there is no gas readings for the meterId = {long}")
    public void checkNoGasMetric(Long meterId) {
        List<Metric> metrics = gasMetricDao.findByMeterIds(
                Collections.singletonList(meterId)
        ).stream().map(MetricEntity::toApiMetric).toList();
        Assertions.assertEquals(0, metrics.size());
    }
}
