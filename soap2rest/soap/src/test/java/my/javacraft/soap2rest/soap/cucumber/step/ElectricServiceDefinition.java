package my.javacraft.soap2rest.soap.cucumber.step;

import io.cucumber.java.en.When;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.soap.generated.ds.ws.DSRequest.Body;
import my.javacraft.soap2rest.soap.generated.ds.ws.DSResponse;
import my.javacraft.soap2rest.soap.generated.ds.ws.KeyValuesType;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrder;
import my.javacraft.soap2rest.soap.service.order.ElectricService;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMethod;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Slf4j
@Scope(SCOPE_CUCUMBER_GLUE)
public class ElectricServiceDefinition extends BaseDefinition {

    @LocalServerPort
    int port;

    ServiceOrder createServiceOrder() {
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setServiceName(ElectricService.class.getSimpleName());
        serviceOrder.setServiceType(RequestMethod.PUT.toString());
        serviceOrder.setServiceOrderID("1");

        List<KeyValuesType> paramsList = serviceOrder.getParams();

        KeyValuesType meterIdValue = new KeyValuesType();
        meterIdValue.setKey("meterId");
        meterIdValue.setValue("100");
        paramsList.add(meterIdValue);

        KeyValuesType readingValue = new KeyValuesType();
        readingValue.setKey("reading");
        readingValue.setValue("678.439");
        paramsList.add(readingValue);

        KeyValuesType dateValue = new KeyValuesType();
        dateValue.setKey("date");
        dateValue.setValue("2023-07-28");
        paramsList.add(dateValue);

        serviceOrder.getParams().addAll(paramsList);

        return serviceOrder;
    }

    @When("we send a SOAP request to submit a new electric metric")
    public void sendSoapRequest() throws Exception {
        Body body = new Body();
        body.setServiceOrder(createServiceOrder());

        DSResponse dsResponse = sendSoapRequest(port, body);

        Assertions.assertNotNull(dsResponse);
        Assertions.assertEquals("200",
                dsResponse.getBody().getServiceOrderStatus().getStatusType().getCode());
        Assertions.assertEquals("Metric(id=13, meterId=100, reading=678.439, date=2023-07-28, usageSinceLastRead=null, periodSinceLastRead=null, avgDailyUsage=null)",
                dsResponse.getBody().getServiceOrderStatus().getStatusType().getResult());
    }

}
