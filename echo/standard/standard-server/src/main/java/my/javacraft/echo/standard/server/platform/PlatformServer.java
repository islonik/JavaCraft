package my.javacraft.echo.standard.server.platform;

import java.io.IOException;
import java.net.Socket;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.echo.standard.server.common.MultithreadedServer;
import my.javacraft.echo.standard.server.common.ServerThread;

@Slf4j
public class PlatformServer extends MultithreadedServer {

    public PlatformServer(int port) {
        super(port);
    }

    @Override
    public void startUpClient(Socket client) {
        try {
            // platform thread
            Thread.ofPlatform()
                    .daemon(true)
                    .start(new ServerThread(client, connectedClients));
        } catch (RuntimeException ex) {
            log.error("Failed to start PlatformServer thread for {}", client, ex);
            try {
                client.close();
            } catch (IOException closeEx) {
                log.error("Could not close client socket after startup failure", closeEx);
            }
        }
    }
}
