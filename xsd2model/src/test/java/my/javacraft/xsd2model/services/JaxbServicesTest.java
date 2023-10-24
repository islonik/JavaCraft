package my.javacraft.xsd2model.services;

import jakarta.xml.bind.JAXBException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xsd2model.model.*;

/**
 * Created by nikilipa on 8/20/16.
 */
public class JaxbServicesTest {

    private JaxbServices coreServices;
    private JaxbServices requestServices;
    private JaxbServices responseServices;
    private UserType userType;

    @Before
    public void setUp() throws JAXBException {
        coreServices = new JaxbServices(UserType.class);
        requestServices = new JaxbServices(RequestType.class);
        responseServices = new JaxbServices(ResponseType.class);

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
        UserType userType2 = (UserType) coreServices.xml2object(xml1);
        // toXml
        String xml2 = coreServices.object2xml(userType2);
        // toXml using different method
        UserType userType3 = (UserType) coreServices.xml2object(xml2);
        String xml3 = coreServices.object2xml2(userType3);

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<ns2:userType xmlns:ns2=\"http://xsd2model.org/examples/xsd2model/model/core\">\n" +
                "    <login>nikita</login>\n" +
                "    <password>password22</password>\n" +
                "</ns2:userType>\n";
        Assert.assertEquals(xml, xml1);
        Assert.assertEquals(xml1, xml2);
        Assert.assertEquals(xml2, xml3);
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
        RequestType requestType2 = (RequestType) requestServices.xml2object(xml1);
        // toXml
        String xml2 = requestServices.object2xml(requestType2);
        // toXml using different method
        RequestType requestType3 = (RequestType) requestServices.xml2object(xml2);
        String xml3 = requestServices.object2xml2(requestType3);

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<ns3:requestType xmlns:ns2=\"http://xsd2model.org/examples/xsd2model/model/core\" xmlns:ns3=\"http://xsd2model.org/examples/xsd2model/model/request\">\n" +
                "    <user>\n" +
                "        <login>nikita</login>\n" +
                "        <password>password22</password>\n" +
                "    </user>\n" +
                "    <message>\n" +
                "        <log>log-1234567</log>\n" +
                "        <command>mkdir test</command>\n" +
                "    </message>\n" +
                "</ns3:requestType>\n";
        Assert.assertEquals(xml, xml1);
        Assert.assertEquals(xml1, xml2);
        Assert.assertEquals(xml2, xml3);
    }

    @Test
    public void testResponseObject2Xml2Object2Xml() throws Exception {
        // object
        ResponseType responseType1 = new ResponseType();
        responseType1.setUser(userType);
        Message message = new Message();
        message.setLog("log-1234567");
        message.setCommand("mkdir test");
        Body body = new Body();
        body.setCommand("rm -r");
        body.setLog("done");
        responseType1.setBody(body);

        // toXml
        String xml1 = responseServices.object2xml(responseType1);
        // toObject
        ResponseType responseType2 = (ResponseType)responseServices.xml2object(xml1);
        // toXml
        String xml2 = responseServices.object2xml(responseType2);
        // toXml using different method
        ResponseType responseType3 = (ResponseType) responseServices.xml2object(xml2);
        String xml3 = responseServices.object2xml2(responseType3);

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<ns3:responseType xmlns:ns2=\"http://xsd2model.org/examples/xsd2model/model/core\" xmlns:ns3=\"http://xsd2model.org/examples/xsd2model/model/response\">\n" +
                "    <user>\n" +
                "        <login>nikita</login>\n" +
                "        <password>password22</password>\n" +
                "    </user>\n" +
                "    <Body>\n" +
                "        <log>done</log>\n" +
                "        <command>rm -r</command>\n" +
                "    </Body>\n" +
                "</ns3:responseType>\n";
        Assert.assertEquals(xml, xml1);
        Assert.assertEquals(xml1, xml2);
        Assert.assertEquals(xml2, xml3);
    }

    @Test
    public void testPerformanceTest() throws Exception {
        final String xmlResponse = new String(Files.readAllBytes(Paths.get("src/test/resources/performance.xml")));
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 150; i++) {
            ResponseType responseType = (ResponseType)responseServices.xml2object(xmlResponse);
            Assert.assertNotNull(responseType.getUser());
            Assert.assertNotNull(responseType.getUser().getLogin());
        }
        long endTime = System.currentTimeMillis();

        long resultTime = endTime - startTime;

        System.out.println("Performance test result time = " + resultTime);
        Assert.assertTrue(resultTime < (1000));
    }

}
