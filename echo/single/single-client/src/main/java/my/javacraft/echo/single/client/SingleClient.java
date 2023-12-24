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

    private final String host;
    private final int port;

    public SingleClient(String host, int port) {
        this.host = host;
        this.port = port;

        singleNetworkManager = new SingleNetworkManager();

        final SingleMessageSender singleMessageSender = new SingleMessageSender();
        singleNetworkManager.setSingleMessageSender(singleMessageSender);

        SingleMessageListener singleMessageListener = new SingleMessageListener(singleNetworkManager);

        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        // loop
        executorService.execute(singleMessageListener);
    }

    public void connectToServer() throws IOException {
        singleNetworkManager.openSocket(host, Integer.toString(port));
        System.out.println("You connected to the server.");
    }

    public void sendMessage(String message) {
        singleNetworkManager.getSingleMessageSender().send(message);
    }

    public String readMessage() {
        return singleNetworkManager.getMessage();
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
                    String inputCommand = keyboard.readLine().trim();

                    sendMessage(inputCommand);

                    if ("bye".equalsIgnoreCase(inputCommand)) {
                        working = false;
                    }
                } catch (IOException e) {
                    System.err.println(e.getLocalizedMessage());
                }
            }
        } catch (Exception error) {
            System.err.println(error.getLocalizedMessage());
        }
        System.exit(1);
    }
}
