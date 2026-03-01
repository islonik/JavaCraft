package my.javacraft.echo.netty.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NettyServerApplicationTest {

    @Test
    void testGetPortWithValidArgument() {
        int port = NettyServerApplication.getPort(new String[]{"9090"});
        Assertions.assertEquals(9090, port);
    }

    @Test
    void testGetPortWithInvalidArgument() {
        int port = NettyServerApplication.getPort(new String[]{"notANumber"});
        Assertions.assertEquals(8076, port, "Invalid port should fall back to DEFAULT_PORT");
    }

    @Test
    void testGetPortWithZero() {
        int port = NettyServerApplication.getPort(new String[]{"0"});
        Assertions.assertEquals(0, port);
    }

    @Test
    void testGetPortWithMaxPort() {
        int port = NettyServerApplication.getPort(new String[]{"65535"});
        Assertions.assertEquals(65535, port);
    }

}
