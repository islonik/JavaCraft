package my.javacraft.echo.standard.client.sync;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;

/**
 * SyncThreadsClient.
 * @author Lipatov Nikita
 * Socket examples: http://www.cs.uic.edu/~troy/spring05/cs450/sockets/socket.html
 */
@Slf4j
public class StandardSyncClient implements Runnable, AutoCloseable {

    private String host;
    private int port;
    private Socket socket;
    private PrintWriter outStream;
    private BufferedReader inStream;

    public StandardSyncClient(String host, int port) {
        try {
            this.host = host;
            this.port = port;

            this.socket = new Socket(host, port);
            this.outStream = new PrintWriter(socket.getOutputStream(), true);

            this.inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            log.info("Sync client {} is connected", socket);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendMessage(String message) {
        outStream.println(message);
    }

    public String readMessage() throws IOException {
        return inStream.readLine();
    }

    public void run() {
        log.info("Starting...");

        try ( // console input stream
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            String userInput;
            while (true) {
                System.out.print("type: ");
                userInput = stdIn.readLine().trim();

                sendMessage(userInput);

                System.out.println(readMessage());
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

    public void close() {
        try {
            if (inStream != null) {
                inStream.close();
            }
            if (outStream != null) {
                outStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
