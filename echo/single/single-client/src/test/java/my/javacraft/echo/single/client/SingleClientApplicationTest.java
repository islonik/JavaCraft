package my.javacraft.echo.single.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SingleClientApplicationTest {

    @Test
    void testGetPortWithValidArgument() {
        int port = SingleClientApplication.getPort(new String[]{"9090"});
        Assertions.assertEquals(9090, port);
    }

    @Test
    void testGetPortWithInvalidArgument() {
        int port = SingleClientApplication.getPort(new String[]{"notANumber"});
        Assertions.assertEquals(SingleClientApplication.DEFAULT_PORT, port,
                "Invalid port should fall back to DEFAULT_PORT");
    }

    @Test
    void testGetPortWithZero() {
        int port = SingleClientApplication.getPort(new String[]{"0"});
        Assertions.assertEquals(0, port);
    }

    @Test
    void testGetPortWithMaxPort() {
        int port = SingleClientApplication.getPort(new String[]{"65535"});
        Assertions.assertEquals(65535, port);
    }

}
