package my.javacraft.echo.standard.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class MultithreadedServer implements Runnable {

    private final int port;
    private final AtomicInteger connectedClients = new AtomicInteger(0);

    public MultithreadedServer(int port) {
        this.port = port;

        log.info("Use next command: telnet localhost " + port);
    }

    @Override
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

                    startUpClient(client);
                }
            }
        } catch (IOException ioe) {
            log.error(ioe.getLocalizedMessage(), ioe);
        }
    }

    private void startUpClient(Socket client) {
        try {
            // we use virtual threads added in Java 21
            Thread.startVirtualThread(new ServerThread(client, connectedClients));
        } catch (RuntimeException ex) {
            log.error("Failed to start server thread for {}", client, ex);
            try {
                client.close();
            } catch (IOException closeEx) {
                log.error("Could not close client socket after startup failure", closeEx);
            }
        }
    }
}
