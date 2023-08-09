package my.javacraft.soap2rest.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetricTest {

    String json = """
    {
        "meterId" : 200,
        "reading" : 2536.708,
        "date" : "2023-04-01"
    }
    """;

    @Test
    public void testJsonDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Metric metric = mapper.readValue(json, Metric.class);
        Assertions.assertNotNull(metric);
    }

}
