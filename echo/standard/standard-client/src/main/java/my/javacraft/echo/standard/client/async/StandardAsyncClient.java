package my.javacraft.echo.standard.client.async;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

/**
 * AsyncThreadsClient
 * @author Lipatov Nikita
 */
@Slf4j
public class StandardAsyncClient {

    private final AsyncClientConnection asyncClientConnection;

    public StandardAsyncClient(String host, int port) {
        this.asyncClientConnection = new AsyncClientConnection(host, port);

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        // loop
        executorService.execute(this.asyncClientConnection);
    }

    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
            log.info("Starting...");
            while (true) {
                String inputText = input.readLine().toLowerCase();

                this.asyncClientConnection.flush(inputText);

                if ("bye".equalsIgnoreCase(inputText)) {
                    break;
                }
            }
        } catch(IOException error) {
            log.error(error.getMessage(), error);
        }
    }
}
