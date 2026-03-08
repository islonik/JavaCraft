package my.javacraft.echo.standard.client.sync;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * StandardSyncClient.
 * <p>
 * @author Lipatov Nikita
 */
@Slf4j
public class StandardSyncClient implements Runnable, AutoCloseable {

    private static final int READ_MESSAGE_TIMEOUT = 5; // seconds
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();

    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter outStream;

    // closedByClient tracks whether close() has been called (by us, intentionally).
    // It guards the close logic so it runs exactly once and makes isConnected() return false after close.
    private final AtomicBoolean closedByClient = new AtomicBoolean(false);

    // closedByServer tracks whether the server closed the connection
    // (detected by the listener thread hitting EOF on readLine()).
    // Tests poll this to know the server has finished processing "bye" and decremented its counter
    // before the next step runs.
    // Sequence:
    // Client sends "bye" → server responds → server closes its side → listener detects EOF → socketClosed = true (server is done)
    @Getter
    private volatile boolean closedByServer = false;

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
                            closedByServer = true;
                        }
                    });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendMessage(String message) {
        if (closedByClient.get() || outStream == null) {
            throw new IllegalStateException("Client is not connected to %s:%d".formatted(host, port));
        }
        outStream.println(message);
    }

    public String readMessage() {
        try {
            return responseQueue.poll(READ_MESSAGE_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public boolean isConnected() {
        return !closedByClient.get() && socket != null && outStream != null;
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
        if (!closedByClient.compareAndSet(false, true)) {
            return;
        }
        try {
            if (outStream != null) {
                outStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            closedByServer = true;
        }
    }
}
