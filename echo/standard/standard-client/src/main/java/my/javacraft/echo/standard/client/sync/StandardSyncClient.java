package my.javacraft.echo.standard.client.sync;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * StandardSyncClient.
 * <p>
 * @author Lipatov Nikita
 */
@Slf4j
public class StandardSyncClient implements Runnable, AutoCloseable {

    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter outStream;
    @Getter
    private volatile boolean socketClosed = false;

    public StandardSyncClient(
            final String threadName,
            final String host,
            final int port) {

        this.host = host;
        this.port = port;

        try {
            this.socket = new Socket(host, port);
            this.outStream = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            log.info("Sync client {} is connected", socket);

            Thread.ofVirtual()
                    .name(threadName + port)
                    .start(() -> {
                        try {
                            String line;
                            while ((line = inStream.readLine()) != null) {
                                responseQueue.add(line);
                            }
                        } catch (SocketException ignored) {
                            // expected when close() is called while blocking on readLine()
                        } catch (IOException e) {
                            log.warn("Listener error: {}", e.getMessage());
                        } finally {
                            socketClosed = true;
                        }
                    });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendMessage(String message) {
        outStream.println(message);
    }

    public String readMessage() {
        try {
            return responseQueue.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public boolean isConnected() {
        return socket != null && outStream != null;
    }

    @Override
    public void run() {
        log.info("Starting...");

        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
            String userInput;
            while (true) {
                System.out.print("type: ");
                String line = stdIn.readLine();
                if (line == null) {
                    // readLine() returns null when the input stream reaches end-of-file (EOF)
                    // for example, Ctrl+D (Unix/Mac) or Ctrl+Z (Windows) — the user signals EOF on the terminal
                    log.info("Detected end-of-file (EOF). Thread terminating...");
                    break;
                }
                userInput = line.trim();

                sendMessage(userInput);

                System.out.println(readMessage());
                if ("bye".equalsIgnoreCase(userInput)) {
                    break;
                }
            }
        } catch (IOException e) {
            log.warn("Couldn't get I/O for the connection to: {}:{}", host, port);
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        try {
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
