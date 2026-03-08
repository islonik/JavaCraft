package my.javacraft.echo.standard.client.sync;

import lombok.extern.slf4j.Slf4j;
import my.javacraft.echo.standard.client.tools.PortValidator;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class StandardSyncClientApplication {

    // telnet localhost 8075
    public static void main(String[] args) {
        int port = PortValidator.getPort(args);

        try (StandardSyncClient syncClient = new StandardSyncClient(
                "sync-client-",
                "localhost",
                port)) {
            syncClient.run();
        }
    }
}
