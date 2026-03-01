package my.javacraft.echo.netty.client;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 * Example was took from official documentation.
 */
@Slf4j
public class NettyClientApplication {

    private static final int DEFAULT_PORT = 8076;

    public static void main(String[] args) throws Exception {
        int port = Optional.of(args)
                .filter(a -> args.length > 0)
                .map(NettyClientApplication::getPort)
                .orElse(DEFAULT_PORT);

        new NettyClient("localhost", port).run();
    }

    private static int getPort(String[] args) {
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            log.warn("Invalid port '{}', using default {}", args[0], DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }
}
