package my.javacraft.echo.single.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SingleServerApplicationTest {

    @Test
    void testGetPortWithValidArgument() {
        int port = SingleServerApplication.getPort(new String[]{"9090"});
        Assertions.assertEquals(9090, port);
    }

    @Test
    void testGetPortWithInvalidArgument() {
        int port = SingleServerApplication.getPort(new String[]{"notANumber"});
        Assertions.assertEquals(SingleServerApplication.DEFAULT_PORT, port,
                "Invalid port should fall back to DEFAULT_PORT");
    }

    @Test
    void testGetPortWithZero() {
        int port = SingleServerApplication.getPort(new String[]{"0"});
        Assertions.assertEquals(0, port);
    }

    @Test
    void testGetPortWithMaxPort() {
        int port = SingleServerApplication.getPort(new String[]{"65535"});
        Assertions.assertEquals(65535, port);
    }

}
