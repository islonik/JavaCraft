package my.javacraft.echo.standard.client.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;

/**
 * SyncThreadsClient.
 * @author Lipatov Nikita
 * Socket examples: http://www.cs.uic.edu/~troy/spring05/cs450/sockets/socket.html
 */
@Slf4j
public class StandardSyncClient {

    private final String host;
    private final int port;

    public StandardSyncClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        log.info("Starting...");

        try (Socket socket = new Socket(host, port);
             PrintWriter outStream = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            log.info("Sync client {} is connected", socket);

            String userInput;

            while (true) {
                System.out.print("type: ");
                userInput = stdIn.readLine().trim();

                outStream.println(userInput);

                System.out.println(inStream.readLine());
                if ("bye".equalsIgnoreCase(userInput)) {
                    break;
                }
            }

        } catch (UnknownHostException e) {
            log.warn("Don't know about host: {}", host);
            System.exit(1);
        } catch (IOException e) {
            log.warn("Couldn't get I/O for the connection to: {}", host);
            System.exit(1);
        }
    }
}
