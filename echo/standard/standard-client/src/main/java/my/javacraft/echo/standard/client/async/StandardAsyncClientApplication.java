package my.javacraft.echo.standard.client.async;

import lombok.extern.slf4j.Slf4j;
import my.javacraft.echo.standard.client.tools.PortValidator;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class StandardAsyncClientApplication {

    // telnet localhost 8075
    public static void main(String[] args) {
        int port = PortValidator.getPort(args);

        try (StandardAsyncClient asyncClient = new StandardAsyncClient(
                "Async-client-application",
                "localhost",
                port)) {
            asyncClient.run();
        }
    }
}
