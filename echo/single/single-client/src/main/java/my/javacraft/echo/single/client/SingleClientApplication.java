package my.javacraft.echo.single.client;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class SingleClientApplication {

    static final int DEFAULT_PORT = 8077;

    // telnet localhost 8077
    public static void main(String[] args) {
        int port = Optional.of(args)
                .filter(a -> a.length > 0)
                .map(SingleClientApplication::getPort)
                .orElse(DEFAULT_PORT);

        // run() already calls close() in its own finally block
        new SingleClient("localhost", port).run();
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
