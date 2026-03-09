package my.javacraft.echo.standard.client.async;

import lombok.extern.slf4j.Slf4j;
import my.javacraft.echo.standard.client.tools.UserClient;

/**
 * StandardAsyncClient
 * <p>
 * @author Lipatov Nikita
 */
@Slf4j
public class StandardAsyncClient extends UserClient implements Runnable, AutoCloseable {

    private final AsyncClientConnection connection;

    public StandardAsyncClient(String threadName, String host, int port) {
        super(host, port);

        this.connection = new AsyncClientConnection(threadName, host, port);
    }

    @Override
    public void sendMessage(String message) {
        connection.sendMessage(message);
    }

    @Override
    public String readMessage() {
        return connection.readMessage();
    }

    @Override
    public boolean isConnected() {
        return connection.isConnected();
    }

    @Override
    public void close() {
        connection.close();
    }

    @Override
    public void run() {
        readUserMessages(log);
    }
}
