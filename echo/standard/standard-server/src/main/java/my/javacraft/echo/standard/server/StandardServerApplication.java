package my.javacraft.echo.standard.server;

import java.util.Optional;

/**
 * @author Lipatov Nikita
 */
public class StandardServerApplication {

    // telnet localhost 8075
    public static void main(String[] args) {
        int port = Optional.of(args)
                .filter(a -> args.length > 0)
                .map(a -> Integer.parseInt(a[0]))
                .orElse(8075);

        new MultithreadedServer(port).run();
    }
}
