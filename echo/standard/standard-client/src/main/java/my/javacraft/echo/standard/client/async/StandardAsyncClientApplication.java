package my.javacraft.echo.standard.client.async;

import java.util.Optional;

/**
 * @author Lipatov Nikita
 */
public class StandardAsyncClientApplication {

    // telnet localhost 8075
    public static void main(String[] args) {
        int port = Optional.of(args)
                .filter(a -> args.length > 0)
                .map(a -> Integer.parseInt(a[0]))
                .orElse(8075);

        new StandardAsyncClient("localhost", port).run();
    }
}
