package my.javacraft.soap2rest.rest.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AsyncRestRequestTest {

    @Test
    public void testToString() {
        AsyncRestRequest request = new AsyncRestRequest("1", "2", "200", "OK");
        Assertions.assertEquals(
                "AsyncRestRequest object where messageId = '1', conversationId = '2', code = '200', desc = 'OK';",
                request.toString()
        );
    }
}
