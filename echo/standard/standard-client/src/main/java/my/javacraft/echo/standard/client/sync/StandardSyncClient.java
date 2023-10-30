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

    public StandardSyncClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }

    public void run() throws IOException {
        Socket socket = null;
        PrintWriter outStream = null;
        BufferedReader inStream = null;

        try {
            socket = new Socket(host, port);
            outStream = new PrintWriter(socket.getOutputStream(), true);
            inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            log.warn(String.format("Don't know about host: %s", host));
            System.exit(1);
        } catch (IOException e) {
            log.warn(String.format("Couldn't get I/O for the connection to: %s", host));
            System.exit(1);
        }
        log.info(String.format("This client %s is connected", socket.toString()));

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;

        while (true) {
            System.out.print("?: ");
            userInput = stdIn.readLine().trim();

            outStream.println(userInput);

            System.out.println(inStream.readLine());
            if ("bye".equalsIgnoreCase(userInput)) {
                break;
            }
        }

        outStream.close();
        inStream.close();
        stdIn.close();
        socket.close();
    }
}
