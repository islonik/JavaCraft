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

        SingleServer server = new SingleServer(port);

        // Without that hook, shutdown signals can leave SingleServer stuck in selector.select() and not exit cleanly.
        //
        // server.stop() does two important things:
        //
        // 1) sets running=false
        // 2) calls selector.wakeup()
        //
        // That lets the event loop in SingleServer.java break promptly instead of waiting forever on I/O readiness.
        //
        // So the hook in SingleServerApplication.java ensures graceful stop on Ctrl+C, JVM termination, or container stop, and avoids hanging shutdown.
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(server);

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
