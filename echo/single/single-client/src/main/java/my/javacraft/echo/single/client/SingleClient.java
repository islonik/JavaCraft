package my.javacraft.echo.single.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lipatov Nikita
 */
public class SingleClient {
    private static final Logger log = LoggerFactory.getLogger(SingleClient.class);
    private final SingleNetworkManager singleNetworkManager;

    private String host;
    private int port;

    public SingleClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;

        singleNetworkManager = new SingleNetworkManager();

        final SingleMessageSender singleMessageSender = new SingleMessageSender();
        singleNetworkManager.setSingleMessageSender(singleMessageSender);

        final SingleMessageListener singleMessageListener = new SingleMessageListener(singleNetworkManager);

        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        // loop
        executorService.execute(singleMessageListener);
    }

    public void run() {
        try
        (
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        ) {
            singleNetworkManager.openSocket(host, Integer.toString(port));
            while (true) {
                try {
                    String inputCommand = keyboard.readLine().trim();

                    singleNetworkManager.getSingleMessageSender().send(inputCommand);
                } catch (IOException e) {
                    System.err.println(e.getLocalizedMessage());
                }
            }
        } catch (Exception error) {
            System.err.println(error.getLocalizedMessage());
        } finally {
            System.exit(1);
        }
    }
}
