package my.javacraft.soap2rest.soap.cucumber.step;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.soap.generated.ds.ws.DSRequest.Body;
import my.javacraft.soap2rest.soap.generated.ds.ws.DSResponse;
import my.javacraft.soap2rest.soap.generated.ds.ws.KeyValuesType;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrder;
import my.javacraft.soap2rest.soap.service.order.GasService;
import my.javacraft.soap2rest.soap.service.order.SmartService;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMethod;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Slf4j
@Scope(SCOPE_CUCUMBER_GLUE)
public class SmartServiceDefinition extends BaseDefinition {

    @LocalServerPort
    int port;

    @When("we send a SOAP request to delete all previous metrics")
    public void sendSoapRequestWithDeleteMethod() throws Exception {
        Body body = new Body();
        body.setServiceOrder(createServiceOrderWithDeleteBody());

        DSResponse dsResponse = sendSoapRequest(port, body);

        Assertions.assertNotNull(dsResponse);
        Assertions.assertEquals("200",
                dsResponse.getBody().getServiceOrderStatus().getStatusType().getCode());
        Assertions.assertEquals("true",
                dsResponse.getBody().getServiceOrderStatus().getStatusType().getResult());
    }

    ServiceOrder createServiceOrderWithDeleteBody() {
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setServiceName(SmartService.class.getSimpleName());
        serviceOrder.setServiceType(RequestMethod.DELETE.toString());
        serviceOrder.setServiceOrderID("1");
        return serviceOrder;
    }

    @When("we send a SOAP request to put new metrics")
    public void sendSoapRequestWithPutMethod() throws Exception {
        Body body = new Body();
        body.setServiceOrder(createServiceOrderWithPutBody());

        DSResponse dsResponse = sendSoapRequest(port, body);

        Assertions.assertNotNull(dsResponse);
        Assertions.assertEquals("200",
                dsResponse.getBody().getServiceOrderStatus().getStatusType().getCode());
        Assertions.assertEquals("true",
                dsResponse.getBody().getServiceOrderStatus().getStatusType().getResult());
    }

    ServiceOrder createServiceOrderWithPutBody() {
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setServiceName(SmartService.class.getSimpleName());
        serviceOrder.setServiceType(RequestMethod.PUT.toString());
        serviceOrder.setServiceOrderID("1");

        List<KeyValuesType> paramsList = serviceOrder.getParams();

        KeyValuesType gasMetric1 = new KeyValuesType();
        gasMetric1.setKey("gasMetric");
        gasMetric1.setValue("""
               {
                    "id" : 23,
                    "meterId" : 200,
                    "reading" : 2531.111,
                    "date" : "2023-07-28"
               }
        """);
        paramsList.add(gasMetric1);

        KeyValuesType gasMetric2 = new KeyValuesType();
        gasMetric2.setKey("gasMetric");
        gasMetric2.setValue("""
               {
                    "id" : 24,
                    "meterId" : 200,
                    "reading" : 2537.777,
                    "date" : "2023-07-29"
               }
        """);
        paramsList.add(gasMetric2);

        KeyValuesType elecMetric1 = new KeyValuesType();
        elecMetric1.setKey("elecMetric");
        elecMetric1.setValue("""
               {
                      "id" : 13,
                      "meterId" : 100,
                      "reading" : 674.444,
                      "date" : "2023-07-28"
               }
        """);
        paramsList.add(elecMetric1);

        KeyValuesType elecMetric2 = new KeyValuesType();
        elecMetric2.setKey("elecMetric");
        elecMetric2.setValue("""
               {
                      "id" : 14,
                      "meterId" : 100,
                      "reading" : 678.888,
                      "date" : "2023-07-29"
               }
        """);
        paramsList.add(elecMetric2);

        return serviceOrder;
    }

    @Then("we send a SOAP request to get the latest metrics")
    public void sendSoapRequestWithGetLatestMethod() throws Exception {
        Body body = new Body();
        body.setServiceOrder(createServiceOrderWithLatestGetBody());

        DSResponse dsResponse = sendSoapRequest(port, body);

        Assertions.assertNotNull(dsResponse);
        Assertions.assertEquals("200",
                dsResponse.getBody().getServiceOrderStatus().getStatusType().getCode());
        Assertions.assertEquals("Metrics(accountId=1, gasReadings=[Metric(id=24, meterId=200, reading=2537.777, date=2023-07-29, usageSinceLastRead=null, periodSinceLastRead=null, avgDailyUsage=null)], elecReadings=[Metric(id=13, meterId=100, reading=678.888, date=2023-07-29, usageSinceLastRead=null, periodSinceLastRead=null, avgDailyUsage=null)])",
                dsResponse.getBody().getServiceOrderStatus().getStatusType().getResult());
    }

    ServiceOrder createServiceOrderWithLatestGetBody() {
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setServiceName(SmartService.class.getSimpleName());
        serviceOrder.setServiceType(RequestMethod.GET.toString());
        serviceOrder.setServiceOrderID("1");

        List<KeyValuesType> paramsList = serviceOrder.getParams();

        KeyValuesType meterIdValue = new KeyValuesType();
        meterIdValue.setKey("path");
        meterIdValue.setValue("/latest");
        paramsList.add(meterIdValue);

        serviceOrder.getParams().addAll(paramsList);

        return serviceOrder;
    }

    @Then("we send a SOAP request to get all metrics")
    public void sendSoapRequestWithGetMethod() throws Exception {
        Body body = new Body();
        body.setServiceOrder(createServiceOrderWithGetBody());

        DSResponse dsResponse = sendSoapRequest(port, body);

        Assertions.assertNotNull(dsResponse);
        Assertions.assertEquals("200",
                dsResponse.getBody().getServiceOrderStatus().getStatusType().getCode());
        Assertions.assertEquals("Metrics(accountId=1, gasReadings=[Metric(id=23, meterId=200, reading=2531.111, date=2023-07-28, usageSinceLastRead=null, periodSinceLastRead=null, avgDailyUsage=null), Metric(id=24, meterId=200, reading=2537.777, date=2023-07-29, usageSinceLastRead=null, periodSinceLastRead=null, avgDailyUsage=null)], elecReadings=[Metric(id=13, meterId=100, reading=674.444, date=2023-07-28, usageSinceLastRead=null, periodSinceLastRead=null, avgDailyUsage=null), Metric(id=13, meterId=100, reading=678.888, date=2023-07-29, usageSinceLastRead=null, periodSinceLastRead=null, avgDailyUsage=null)])",
                dsResponse.getBody().getServiceOrderStatus().getStatusType().getResult());
    }

    ServiceOrder createServiceOrderWithGetBody() {
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setServiceName(SmartService.class.getSimpleName());
        serviceOrder.setServiceType(RequestMethod.GET.toString());
        serviceOrder.setServiceOrderID("1");

        List<KeyValuesType> paramsList = serviceOrder.getParams();

        KeyValuesType meterIdValue = new KeyValuesType();
        meterIdValue.setKey("path");
        meterIdValue.setValue("");
        paramsList.add(meterIdValue);

        serviceOrder.getParams().addAll(paramsList);

        return serviceOrder;
    }
}
