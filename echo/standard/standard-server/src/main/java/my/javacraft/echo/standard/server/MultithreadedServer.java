package my.javacraft.echo.standard.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lipatov Nikita
 */
public class MultithreadedServer {
    private static final Logger log = LoggerFactory.getLogger(MultithreadedServer.class);

    private static int port = -1;

    public MultithreadedServer(int port) {
        this.port = port;

        System.out.println("Use next command: telnet localhost " + port);
    }

    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);

            String serverHello = "\n" +
                    "Server host address - " + server.getInetAddress().getHostAddress() + "\n" +
                    "Server host name - " + server.getInetAddress().getHostName() + "\n" +
                    "Server port - " + server.getLocalPort() + "\n";

            log.info(serverHello);

            while (true) {
                Socket client = server.accept();
                if (client != null) {
                    String info = String.format("New client from %s is connected", client.toString());

                    log.info(info);

                    ServerThread handler = new ServerThread(client);
                    handler.start();
                }
            }
        } catch (IOException ioe) {
            log.error(ioe.getLocalizedMessage(), ioe);
            System.out.println(ioe.getMessage());
        }
    }
}
