package my.javacraft.echo.standard.server.virtual;

import java.io.IOException;
import java.net.Socket;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.echo.standard.server.common.ServerThread;
import my.javacraft.echo.standard.server.common.MultithreadedServer;

@Slf4j
public class VirtualServer extends MultithreadedServer {

    public VirtualServer(int port) {
        super(port);
    }

    @Override
    public void startUpClient(Socket client) {
        try {
            // we use virtual threads added in Java 21
            Thread.startVirtualThread(new ServerThread(client, connectedClients));
        } catch (RuntimeException ex) {
            log.error("Failed to start VirtualServer thread for {}", client, ex);
            try {
                client.close();
            } catch (IOException closeEx) {
                log.error("Could not close client socket after startup failure", closeEx);
            }
        }
    }

}
