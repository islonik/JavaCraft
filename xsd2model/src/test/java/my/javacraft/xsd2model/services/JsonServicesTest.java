package my.javacraft.xsd2model.services;

import org.xsd2model.model.ResponseType;
import org.xsd2model.model.UserType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by nikilipa on 8/20/16.
 */
public class JsonServicesTest {

    private UserType userType;

    @BeforeEach
    public void setUp() {
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
        String actualJson = JsonServices.objectToJson(responseType0);
        // toObject
        ResponseType responseType2 = (ResponseType) JsonServices.jsonToObject(actualJson, ResponseType.class);
        // toJson
        String actualJson2 = JsonServices.objectToJson(responseType2);

        Assertions.assertEquals(actualJson, actualJson2);
        String expectedJson = """
                {
                  "user" : {
                    "login" : "nikita",
                    "password" : "password22"
                  },
                  "code" : "0",
                  "desc" : "Ok!"
                }""";
        Assertions.assertEquals(
                expectedJson.replaceAll("\r", "\n"),
                actualJson.replaceAll("\r\n", "\n")
        );
    }

}
