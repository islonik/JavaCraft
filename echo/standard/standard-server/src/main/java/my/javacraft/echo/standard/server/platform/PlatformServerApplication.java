package my.javacraft.echo.standard.server.platform;

import my.javacraft.echo.standard.server.common.PortValidator;

/**
 * @author Lipatov Nikita
 */
public class PlatformServerApplication {
    // telnet localhost 8075
    public static void main(String[] args) {
        int port = PortValidator.getPort(args);

        new PlatformServer(port).run();
    }
}
