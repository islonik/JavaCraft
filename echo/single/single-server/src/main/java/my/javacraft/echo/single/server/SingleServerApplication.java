package my.javacraft.echo.single.server;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Lipatov Nikita
 */
public class SingleServerApplication {

    // telnet localhost 8077
    public static void main(String[] args) throws Exception {
        int port = Optional.of(args)
                .filter(a -> args.length > 0)
                .map(a -> Integer.parseInt(a[0]))
                .orElse(8077);

        ExecutorService es = Executors.newFixedThreadPool(1);
        es.submit(new SingleServer(port));

        es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        es.shutdown();
    }


}
