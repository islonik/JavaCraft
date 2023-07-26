package my.javacraft.soap2rest.rest.app.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResponseGeneratorServicesTest {

    @Test
    public void testAckResponseCase01() {
        ResponseGeneratorServices responseGeneratorServices = new ResponseGeneratorServices();
        Assertions.assertEquals(
                """
                        {
                            "status": "Acknowledgement"
                        }
                        """,
                responseGeneratorServices.getAckResponse()
        );
    }

    @Test
    public void testSimpleJsonErrorCase01() {
        ResponseGeneratorServices responseGeneratorServices = new ResponseGeneratorServices();
        Assertions.assertEquals(
                """
                        {
                            "error": {
                                "code": "304",
                                "message": "Hell O"
                            }
                        }
                        """,
                responseGeneratorServices.getSimpleJsonError("304", "Hell O")
        );
    }
}
