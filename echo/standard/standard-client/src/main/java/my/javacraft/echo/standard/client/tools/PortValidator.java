package my.javacraft.echo.standard.client.tools;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortValidator {
    static final int DEFAULT_PORT = 8075;

    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65_535;

    public static int getPort(String[] args) {
        if (args == null || args.length == 0) {
            return DEFAULT_PORT;
        }

        try {
            String rawPort = args[0];
            int port = Integer.parseInt(rawPort);
            if (port < MIN_PORT || port > MAX_PORT) {
                log.warn("Port '{}' is out of range, using default {}", rawPort, DEFAULT_PORT);
                return DEFAULT_PORT;
            }
            return port;
        } catch (NumberFormatException e) {
            log.warn("Invalid port '{}', using default {}", args[0], DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }
}
