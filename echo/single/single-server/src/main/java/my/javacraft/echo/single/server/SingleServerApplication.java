package my.javacraft.echo.single.server;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class SingleServerApplication {

    static final int DEFAULT_PORT = 8077;

    // telnet localhost 8077
    public static void main(String[] args) throws Exception {
        int port = Optional.of(args)
                .filter(a -> a.length > 0)
                .map(SingleServerApplication::getPort)
                .orElse(DEFAULT_PORT);

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(new SingleServer(port));

        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
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
