package my.javacraft.xsd2model.services;

import jakarta.xml.bind.JAXBException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xsd2model.model.ResponseType;
import org.xsd2model.model.UserType;


/**
 * Created by nikilipa on 8/20/16.
 */
public class JsonServicesTest {

    private UserType userType;

    @Before
    public void setUp() throws JAXBException {
        UserType userType = new UserType();
        userType.setLogin("nikita");
        userType.setPassword("password22");
        this.userType = userType;
    }

    @Test
    public void testObject2Json2Object2Json() throws Exception {
        // object
        ResponseType responseType0 = new ResponseType();
        responseType0.setUser(userType);
        responseType0.setCode("0");
        responseType0.setDesc("Ok!");

        // toJson
        String json1 = JsonServices.objectToJson(responseType0);
        // toObject
        ResponseType responseType2 = (ResponseType) JsonServices.jsonToObject(json1, ResponseType.class);
        // toJson
        String json2 = JsonServices.objectToJson(responseType2);

        Assert.assertEquals(json1, json2);
        String json = "{\n" +
                "  \"user\" : {\n" +
                "    \"login\" : \"nikita\",\n" +
                "    \"password\" : \"password22\"\n" +
                "  },\n" +
                "  \"code\" : \"0\",\n" +
                "  \"desc\" : \"Ok!\"\n" +
                "}";
        Assert.assertEquals(json.replaceAll("\r", "\n"), json1.replaceAll("\r\n", "\n"));
    }

}
