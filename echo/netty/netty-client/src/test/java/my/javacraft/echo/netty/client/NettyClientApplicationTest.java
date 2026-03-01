package my.javacraft.echo.netty.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NettyClientApplicationTest {

    @Test
    void testGetPortWithValidArgument() {
        int port = NettyClientApplication.getPort(new String[]{"9090"});
        Assertions.assertEquals(9090, port);
    }

    @Test
    void testGetPortWithInvalidArgument() {
        int port = NettyClientApplication.getPort(new String[]{"notANumber"});
        Assertions.assertEquals(8076, port, "Invalid port should fall back to DEFAULT_PORT");
    }

    @Test
    void testGetPortWithZero() {
        int port = NettyClientApplication.getPort(new String[]{"0"});
        Assertions.assertEquals(0, port);
    }

    @Test
    void testGetPortWithMaxPort() {
        int port = NettyClientApplication.getPort(new String[]{"65535"});
        Assertions.assertEquals(65535, port);
    }

}
