package my.javacraft.echo.standard.client.async;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;

/**
 * AsyncThreadsClient
 * @author Lipatov Nikita
 */
@Slf4j
public class StandardAsyncClient implements AutoCloseable {

    private final AsyncClientConnection connection;

    public StandardAsyncClient(String threadName, String host, int port) {
        this.connection = new AsyncClientConnection(threadName, host, port);
    }

    public void sendMessage(String message) {
        connection.sendMessage(message);
    }

    public String readMessage() {
        return connection.readMessage();
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    public boolean isSocketClosed() {
        return connection.isSocketClosed();
    }

    @Override
    public void close() {
        connection.close();
    }

    public void run() {
        log.info("Starting...");
        try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String inputText = input.readLine().toLowerCase();
                sendMessage(inputText);
                System.out.println(readMessage());
                if ("bye".equalsIgnoreCase(inputText)) {
                    break;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            close();
        }
    }
}
