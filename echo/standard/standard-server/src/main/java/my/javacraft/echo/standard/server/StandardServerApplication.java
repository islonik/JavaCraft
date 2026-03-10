package my.javacraft.echo.standard.server;

import lombok.extern.slf4j.Slf4j;
import my.javacraft.echo.standard.server.common.PortValidator;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class StandardServerApplication {

    // telnet localhost 8075
    public static void main(String[] args) {
        int port = PortValidator.getPort(args);

        new MultithreadedServer(port).run();
    }

}
