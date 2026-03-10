package my.javacraft.echo.blocking.server.virtual;

import java.net.Socket;
import my.javacraft.echo.blocking.server.common.ServerThread;
import my.javacraft.echo.blocking.server.common.MultithreadedServer;

public class VirtualServer extends MultithreadedServer {

    public VirtualServer(int port) {
        super(port);
    }

    @Override
    public void startUpClient(Socket client) {
        // we use virtual threads added in Java 21
        Thread.startVirtualThread(new ServerThread(client, connectedClients));
    }

}
