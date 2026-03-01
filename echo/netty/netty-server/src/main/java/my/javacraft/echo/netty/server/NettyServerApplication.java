package my.javacraft.echo.netty.server;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 * Example was taken from official documentation.
 */
@Slf4j
public class NettyServerApplication {

    private static final int DEFAULT_PORT = 8076;

    // telnet localhost 8076
    public static void main(String[] args) throws Exception {
        int port = Optional.of(args)
                .filter(a -> a.length > 0)
                .map(NettyServerApplication::getPort)
                .orElse(DEFAULT_PORT);

        new NettyServer(port).run();
    }

    static int getPort(String[] args) {
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            log.warn("Invalid port '{}', using default {}", args[0], DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }

}
