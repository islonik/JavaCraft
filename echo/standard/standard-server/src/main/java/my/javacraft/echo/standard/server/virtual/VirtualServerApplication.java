package my.javacraft.echo.standard.server.virtual;

import lombok.extern.slf4j.Slf4j;
import my.javacraft.echo.standard.server.common.PortValidator;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class VirtualServerApplication {

    // telnet localhost 8075
    public static void main(String[] args) {
        int port = PortValidator.getPort(args);

        try (VirtualServer server = new VirtualServer(port)) {
            server.run();
        }
    }

}
