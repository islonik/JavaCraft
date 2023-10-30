package my.javacraft.xsd2model.services;

import jakarta.xml.bind.JAXBException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xsd2model.model.*;

/**
 * Created by nikilipa on 8/20/16.
 */
@Slf4j
public class JaxbServicesTest {

    private JaxbServices<UserType> coreServices;
    private JaxbServices<RequestType> requestServices;
    private JaxbServices<ResponseType> responseServices;
    private UserType userType;

    @BeforeEach
    public void setUp() throws JAXBException {
        coreServices = new JaxbServices<>(UserType.class);
        requestServices = new JaxbServices<>(RequestType.class);
        responseServices = new JaxbServices<>(ResponseType.class);

        UserType userType = new UserType();
        userType.setLogin("nikita");
        userType.setPassword("password22");
        this.userType = userType;
    }

    @Test
    public void testCoreObject2Xml2Object2Xml() throws Exception {

        // toXml
        String xml1 = coreServices.object2xml(this.userType);
        // toObject
        UserType userType2 = coreServices.xml2object(xml1);
        // toXml
        String xml2 = coreServices.object2xml(userType2);
        // toXml using different method
        UserType userType3 = coreServices.xml2object(xml2);
        String xml3 = coreServices.object2xml2(userType3);

        String xml = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <ns2:userType xmlns:ns2="http://xsd2model.org/core">
                    <login>nikita</login>
                    <password>password22</password>
                </ns2:userType>
                """;
        Assertions.assertEquals(xml, xml1);
        Assertions.assertEquals(xml1, xml2);
        Assertions.assertEquals(xml2, xml3);
    }

    @Test
    public void testRequestObject2Xml2Object2Xml() throws Exception {
        // object
        RequestType requestType1 = new RequestType();
        requestType1.setUser(userType);
        Message message = new Message();
        message.setLog("log-1234567");
        message.setCommand("mkdir test");
        requestType1.setMessage(message);

        // toXml
        String xml1 = requestServices.object2xml(requestType1);
        // toObject
        RequestType requestType2 = requestServices.xml2object(xml1);
        // toXml
        String xml2 = requestServices.object2xml(requestType2);
        // toXml using different method
        RequestType requestType3 = requestServices.xml2object(xml2);
        String xml3 = requestServices.object2xml2(requestType3);

        String xml = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <ns3:requestType xmlns:ns2="http://xsd2model.org/core" xmlns:ns3="http://xsd2model.org/request">
                    <user>
                        <login>nikita</login>
                        <password>password22</password>
                    </user>
                    <message>
                        <log>log-1234567</log>
                        <command>mkdir test</command>
                    </message>
                </ns3:requestType>
                """;
        Assertions.assertEquals(xml, xml1);
        Assertions.assertEquals(xml1, xml2);
        Assertions.assertEquals(xml2, xml3);
    }

    @Test
    public void testResponseObject2Xml2Object2Xml() throws Exception {
        // object
        ResponseType responseType1 = new ResponseType();
        responseType1.setUser(userType);
        Body body = new Body();
        body.setCommand("rm -r");
        body.setLog("done");
        responseType1.setBody(body);

        // toXml
        String xml1 = responseServices.object2xml(responseType1);
        // toObject
        ResponseType responseType2 = responseServices.xml2object(xml1);
        // toXml
        String xml2 = responseServices.object2xml(responseType2);
        // toXml using different method
        ResponseType responseType3 = responseServices.xml2object(xml2);
        String xml3 = responseServices.object2xml2(responseType3);

        String xml = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <ns3:responseType xmlns:ns2="http://xsd2model.org/core" xmlns:ns3="http://xsd2model.org/response">
                    <user>
                        <login>nikita</login>
                        <password>password22</password>
                    </user>
                    <Body>
                        <log>done</log>
                        <command>rm -r</command>
                    </Body>
                </ns3:responseType>
                """;
        Assertions.assertEquals(xml, xml1);
        Assertions.assertEquals(xml1, xml2);
        Assertions.assertEquals(xml2, xml3);
    }

    @Test
    public void testPerformanceTest() throws Exception {
        final String xmlResponse = new String(Files.readAllBytes(Paths.get("src/test/resources/performance.xml")));
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 150; i++) {
            ResponseType responseType = responseServices.xml2object(xmlResponse);
            Assertions.assertNotNull(responseType.getUser());
            Assertions.assertNotNull(responseType.getUser().getLogin());
        }
        long endTime = System.currentTimeMillis();

        long resultTime = endTime - startTime;

        log.info("Performance test result time = " + resultTime);
        Assertions.assertTrue(resultTime < (1000));
    }

}
