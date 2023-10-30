package my.javacraft.echo.standard.client.sync;

import java.io.IOException;

/**
 * @author Lipatov Nikita
 */
public class StandardSyncClientApplication {

    // telnet localhost 8075
    public static void main(String[] args) throws IOException {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8075;
        }

        new StandardSyncClient("localhost", port).run();
    }
}
