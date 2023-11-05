package my.javacraft.echo.standard.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class MultithreadedServer {

    private final int port;

    public MultithreadedServer(int port) {
        this.port = port;

        log.info("Use next command: telnet localhost " + port);
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {

            String serverHello = """ 
                    \\{^_^}/ Hi!
                    *********************************************
                    Server stats:
                    Server canonical host name - %s
                    Server host address - %s
                    Server host name - %s
                    Server port - %s
                    *********************************************
                    """.formatted(
                    server.getInetAddress().getCanonicalHostName(),
                    server.getInetAddress().getHostAddress(),
                    server.getInetAddress().getHostName(),
                    server.getLocalPort()
            );

            log.info(serverHello);

            while (true) {
                Socket client = server.accept();
                if (client != null) {
                    String info = String.format("New client from %s is connected", client);

                    log.info(info);

                    ServerThread handler = new ServerThread(client);
                    handler.start();
                }
            }
        } catch (IOException ioe) {
            log.error(ioe.getLocalizedMessage(), ioe);
        }
    }
}
