package my.javacraft.soap2rest.soap.cucumber.step;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.cucumber.java.en.When;
import jakarta.xml.soap.MessageFactory;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.soap.generated.ds.ws.*;
import my.javacraft.soap2rest.soap.generated.ds.ws.DSRequest.Body;
import my.javacraft.soap2rest.soap.service.HttpCallService;
import my.javacraft.soap2rest.soap.service.order.MetricService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Scope;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

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

    ServiceOrder createServiceOrder() {
        ServiceOrder serviceOrder = new ServiceOrder();

        List<KeyValuesType> paramsList = serviceOrder.getParams();

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

        serviceOrder.setServiceType("gas");
        serviceOrder.setServiceOrderID("1");
        serviceOrder.getParams().addAll(paramsList);

        return serviceOrder;
    }

    @When("we submit a new gas metric")
    public void submitGasMetric() throws JsonProcessingException {
        ServiceOrderStatus sos = metricService.process(createServiceOrder());

        Assertions.assertEquals("200", sos.getStatusType().getCode());
        Assertions.assertEquals("OK", sos.getStatusType().getResult());
    }

    @When("we send SOAP request")
    public void sendSoapRequest() throws Exception {
        SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
        messageFactory.afterPropertiesSet();

        WebServiceTemplate webServiceTemplate = new WebServiceTemplate(messageFactory);

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("my.javacraft.soap2rest.soap.generated.ds.ws");
        marshaller.afterPropertiesSet();

        webServiceTemplate.setMarshaller(marshaller);
        webServiceTemplate.setUnmarshaller(marshaller);
        webServiceTemplate.afterPropertiesSet();

        Body body = new Body();
        body.setServiceOrder(createServiceOrder());

        DSRequest dsRequest = new DSRequest();
        dsRequest.setBody(body);

        DSResponse dsResponse = (DSResponse) webServiceTemplate.marshalSendAndReceive(
                "http://localhost:" + port + "/soap2rest/soap/v1/DeliverServiceWS.wsdl",
                dsRequest
        );
        Assertions.assertNotNull(dsResponse);
    }

}
