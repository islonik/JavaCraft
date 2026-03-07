package my.javacraft.soap2rest.soap.cucumber.step;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import my.javacraft.soap2rest.soap.generated.ds.ws.DSRequest.Body;
import my.javacraft.soap2rest.soap.generated.ds.ws.DSResponse;
import my.javacraft.soap2rest.soap.generated.ds.ws.KeyValuesType;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrder;
import my.javacraft.soap2rest.soap.service.order.SmartService;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMethod;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Scope(SCOPE_CUCUMBER_GLUE)
public class SmartServiceDefinition extends BaseDefinition {

    private static final String SUCCESS_CODE = "200";

    @LocalServerPort
    int port;

    @When("user {string} deletes all previous smart metrics")
    public void userDeletesAllPreviousSmartMetrics(String accountId) throws Exception {
        DSResponse dsResponse = sendServiceOrder(createServiceOrder(RequestMethod.DELETE.toString(), accountId));
        assertStatusAndResult(dsResponse, SUCCESS_CODE, "true");
    }

    @Then("user {string} has no latest smart metrics")
    public void userHasNoLatestSmartMetrics(String accountId) throws Exception {
        ServiceOrder serviceOrder = createServiceOrder(RequestMethod.GET.toString(), accountId);
        addParam(serviceOrder, "path", "/latest");

        DSResponse dsResponse = sendServiceOrder(serviceOrder);
        assertStatusCode(dsResponse, SUCCESS_CODE);
        Assertions.assertNull(getStatusResult(dsResponse));
    }

    @When(
            "user {string} puts smart metrics gas id {string} meter {string} reading {string} date {string} "
                    + "electric id {string} meter {string} reading {string} date {string}")
    public void userPutsSmartMetrics(
            String accountId,
            String gasId,
            String gasMeterId,
            String gasReading,
            String gasDate,
            String elecId,
            String elecMeterId,
            String elecReading,
            String elecDate
    ) throws Exception {
        ServiceOrder serviceOrder = createServiceOrder(RequestMethod.PUT.toString(), accountId);
        addMetricParam(serviceOrder, "gasMetric", gasId, gasMeterId, gasReading, gasDate);
        addMetricParam(serviceOrder, "elecMetric", elecId, elecMeterId, elecReading, elecDate);

        DSResponse dsResponse = sendServiceOrder(serviceOrder);
        assertStatusAndResult(dsResponse, SUCCESS_CODE, "true");
    }

    @When("user {string} puts smart metrics")
    public void userPutsSmartMetrics(String accountId, DataTable table) throws Exception {
        for (Map<String, String> row : table.asMaps(String.class, String.class)) {
            userPutsSmartMetrics(
                    accountId,
                    row.get("gasId"),
                    row.get("gasMeterId"),
                    row.get("gasReading"),
                    row.get("gasDate"),
                    row.get("elecId"),
                    row.get("elecMeterId"),
                    row.get("elecReading"),
                    row.get("elecDate")
            );
        }
    }

    @Then(
            "user {string} gets latest smart metrics gas id {string} meter {string} reading {string} date {string} "
                    + "electric id {string} meter {string} reading {string} date {string}")
    public void userGetsLatestSmartMetrics(
            String accountId,
            String gasId,
            String gasMeterId,
            String gasReading,
            String gasDate,
            String elecId,
            String elecMeterId,
            String elecReading,
            String elecDate
    ) throws Exception {
        ServiceOrder serviceOrder = createServiceOrder(RequestMethod.GET.toString(), accountId);
        addParam(serviceOrder, "path", "/latest");

        DSResponse dsResponse = sendServiceOrder(serviceOrder);
        assertStatusAndResult(
                dsResponse,
                SUCCESS_CODE,
                toMetricsResult(
                        accountId,
                        toMetricResult(gasId, gasMeterId, gasReading, gasDate),
                        toMetricResult(elecId, elecMeterId, elecReading, elecDate)
                )
        );
    }

    @Then("user {string} has smart metrics list size {string}")
    public void userHasSmartMetricsListSize(String accountId, String expectedSize) throws Exception {
        ServiceOrder serviceOrder = createServiceOrder(RequestMethod.GET.toString(), accountId);
        addParam(serviceOrder, "path", "");

        DSResponse dsResponse = sendServiceOrder(serviceOrder);
        assertStatusCode(dsResponse, SUCCESS_CODE);

        int expected = Integer.parseInt(expectedSize);
        String statusResult = getStatusResult(dsResponse);
        Assertions.assertEquals(expected, toMetricsListSize(statusResult, "gasReadings=[", "], elecReadings=["));
        Assertions.assertEquals(expected, toMetricsListSize(statusResult, "elecReadings=[", "])"));
    }

    ServiceOrder createServiceOrder(String serviceType, String accountId) {
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setServiceName(SmartService.class.getSimpleName());
        serviceOrder.setServiceType(serviceType);
        serviceOrder.setServiceOrderID(accountId);
        return serviceOrder;
    }

    void addParam(ServiceOrder serviceOrder, String key, String value) {
        KeyValuesType keyValuesType = new KeyValuesType();
        keyValuesType.setKey(key);
        keyValuesType.setValue(value);
        serviceOrder.getParams().add(keyValuesType);
    }

    void addMetricParam(
            ServiceOrder serviceOrder, String key, String id, String meterId, String reading, String date) {
        addParam(serviceOrder, key, toMetricJson(id, meterId, reading, date));
    }

    DSResponse sendServiceOrder(ServiceOrder serviceOrder) throws Exception {
        Body body = new Body();
        body.setServiceOrder(serviceOrder);
        return sendSoapRequest(port, body);
    }

    void assertStatusAndResult(DSResponse dsResponse, String expectedCode, String expectedResult) {
        assertStatusCode(dsResponse, expectedCode);
        Assertions.assertEquals(expectedResult, getStatusResult(dsResponse));
    }

    void assertStatusCode(DSResponse dsResponse, String expectedCode) {
        Assertions.assertNotNull(dsResponse);
        Assertions.assertEquals(expectedCode, dsResponse.getBody().getServiceOrderStatus().getStatusType().getCode());
    }

    String getStatusResult(DSResponse dsResponse) {
        return dsResponse.getBody().getServiceOrderStatus().getStatusType().getResult();
    }

    String toMetricJson(String id, String meterId, String reading, String date) {
        return "{\"id\":%s,\"meterId\":%s,\"reading\":%s,\"date\":\"%s\"}"
                .formatted(id, meterId, reading, date);
    }

    String toMetricResult(String metricId, String meterId, String reading, String date) {
        return "Metric(id=%s, meterId=%s, reading=%s, date=%s, usageSinceLastRead=null, periodSinceLastRead=null, avgDailyUsage=null)"
                .formatted(metricId, meterId, reading, date);
    }

    String toMetricsResult(String accountId, String gasMetricResult, String electricMetricResult) {
        return "Metrics(accountId=%s, gasReadings=[%s], elecReadings=[%s])"
                .formatted(accountId, gasMetricResult, electricMetricResult);
    }

    int toMetricsListSize(String statusResult, String sectionStart, String sectionEnd) {
        if (statusResult == null || statusResult.isBlank()) {
            return 0;
        }

        int start = statusResult.indexOf(sectionStart);
        if (start < 0) {
            return 0;
        }
        start += sectionStart.length();

        int end = statusResult.indexOf(sectionEnd, start);
        if (end < 0) {
            return 0;
        }

        String section = statusResult.substring(start, end);
        if (section.isBlank()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = section.indexOf("Metric(", index)) >= 0) {
            count++;
            index += "Metric(".length();
        }
        return count;
    }
}
