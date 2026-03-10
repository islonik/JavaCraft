package my.javacraft.echo.standard.server.common;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public abstract class MultithreadedServer implements Runnable {

    protected final AtomicInteger connectedClients = new AtomicInteger(0);
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final int port;

    public MultithreadedServer(int port) {
        this.port = port;

        log.info("Use next command: telnet localhost {}", port);
    }

    @Override
    public void run() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Server on port {} is already running", port);
            return;
        }

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

            while (running.get()) {
                Socket client = server.accept();

                String info = String.format("New client from '%s' is connected", client);
                log.info(info);

                // we handle all exceptions internally
                startUpClient(client);
            }
        } catch (SocketException se) {
            if (running.get()) {
                log.error("Server socket failure on port {}", port, se);
            }
        } catch (IOException ioe) {
            if (running.get()) {
                log.error(ioe.getLocalizedMessage(), ioe);
            }
        } finally {
            running.set(false);
        }
    }

    public abstract void startUpClient(Socket client);
}
