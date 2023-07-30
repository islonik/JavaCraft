package my.javacraft.soap2rest.rest.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.javacraft.soap2rest.rest.api.Metrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SmartServiceTest {

    String json = """
    {
      "accountId" : 1,
      "gasReadings" : [{
        "meterId" : 100,
        "reading" : 700.502,
        "date" : 1689807600000
      } ],
      "electricReadings" : [ {
        "meterId" : 200,
        "reading" : 2536.708,
        "date" : 1689807600000
      } ]
    }
    """;

    @Test
    public void testJsonDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Metrics metrics = mapper.readValue(json, Metrics.class);
        Assertions.assertNotNull(metrics);
    }
}
