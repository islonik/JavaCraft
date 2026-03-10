package my.javacraft.echo.standard.server.platform;

import java.net.Socket;
import my.javacraft.echo.standard.server.common.MultithreadedServer;
import my.javacraft.echo.standard.server.common.ServerThread;

public class PlatformServer extends MultithreadedServer {

    public PlatformServer(int port) {
        super(port);
    }

    @Override
    public void startUpClient(Socket client) {
        // platform thread
        Thread.ofPlatform()
                .daemon(true)
                .start(new ServerThread(client, connectedClients));
    }
}
