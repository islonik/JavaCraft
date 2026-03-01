package my.javacraft.echo.single.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class SingleClient {
    private final SingleNetworkManager singleNetworkManager;
    private final ExecutorService listenerExecutor;

    private final String host;
    private final int port;

    public SingleClient(String host, int port) {
        this.host = host;
        this.port = port;

        singleNetworkManager = new SingleNetworkManager();

        final SingleMessageSender singleMessageSender = new SingleMessageSender();
        singleNetworkManager.setSingleMessageSender(singleMessageSender);

        SingleMessageListener singleMessageListener = new SingleMessageListener(singleNetworkManager);

        listenerExecutor = Executors.newSingleThreadExecutor();
        listenerExecutor.execute(singleMessageListener);
    }

    public void connectToServer() throws IOException {
        singleNetworkManager.openSocket(host, port);
        log.info("You connected to the server.");
    }

    public void sendMessage(String message) {
        singleNetworkManager.getSingleMessageSender().send(message);
    }

    public String readMessage() {
        return singleNetworkManager.getMessage();
    }

    public void close() {
        listenerExecutor.shutdownNow();
        singleNetworkManager.closeSocket();
    }

    public void run() {
        try
        (
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in))
        ) {
            connectToServer();

            boolean working = true;
            while (working) {
                try {
                    String line = keyboard.readLine();
                    if (line == null) {
                        break;
                    }
                    String inputCommand = line.trim();

                    sendMessage(inputCommand);

                    if ("bye".equalsIgnoreCase(inputCommand)) {
                        working = false;
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (Exception error) {
            log.error(error.getMessage(), error);
        } finally {
            close();
        }
    }
}
