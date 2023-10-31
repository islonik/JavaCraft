package my.javacraft.echo.standard.client.sync;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Lipatov Nikita
 */
public class StandardSyncClientApplication {

    // telnet localhost 8075
    public static void main(String[] args) throws IOException {
        int port = Optional.of(args)
                .filter(a -> args.length > 0)
                .map(a -> Integer.parseInt(a[0]))
                .orElse(8075);

        new StandardSyncClient("localhost", port).run();
    }
}
