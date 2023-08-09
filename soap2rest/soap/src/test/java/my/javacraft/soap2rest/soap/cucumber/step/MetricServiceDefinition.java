package my.javacraft.soap2rest.soap.cucumber.step;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.cucumber.java.en.When;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.soap.generated.ds.ws.KeyValuesType;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrder;
import my.javacraft.soap2rest.soap.generated.ds.ws.ServiceOrderStatus;
import my.javacraft.soap2rest.soap.service.HttpCallService;
import my.javacraft.soap2rest.soap.service.order.MetricService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

/**
 * This test shows how to create HTTP requests <b>without</b> <i>io.rest-assured:rest-assured</i> library.
 */
@Slf4j
@Scope(SCOPE_CUCUMBER_GLUE)
public class MetricServiceDefinition {

    @LocalServerPort
    int port;

    @Autowired
    private HttpCallService httpCallService;

    @Autowired
    private MetricService metricService;

    @When("we submit a new gas metric")
    public void submitGasMetric() throws JsonProcessingException {
        ServiceOrder serviceOrder = new ServiceOrder();
        List<KeyValuesType> paramsList = serviceOrder.getParams();

        KeyValuesType accountIdValue = new KeyValuesType();
        accountIdValue.setKey("accountId");
        accountIdValue.setValue("1");
        paramsList.add(accountIdValue);

        KeyValuesType typeValue = new KeyValuesType();
        typeValue.setKey("type");
        typeValue.setValue("gas");
        paramsList.add(typeValue);

        KeyValuesType meterIdValue = new KeyValuesType();
        meterIdValue.setKey("meterId");
        meterIdValue.setValue("200");
        paramsList.add(meterIdValue);

        KeyValuesType readingValue = new KeyValuesType();
        readingValue.setKey("reading");
        readingValue.setValue("56");
        paramsList.add(readingValue);

        KeyValuesType dateValue = new KeyValuesType();
        dateValue.setKey("date");
        dateValue.setValue("2023-07-28");
        paramsList.add(dateValue);

        serviceOrder.getParams().addAll(paramsList);

        ServiceOrderStatus sos = metricService.process(serviceOrder);

        Assertions.assertEquals("200", sos.getStatusType().getCode());
        Assertions.assertEquals("Ok", sos.getStatusType().getResult());
    }

}
