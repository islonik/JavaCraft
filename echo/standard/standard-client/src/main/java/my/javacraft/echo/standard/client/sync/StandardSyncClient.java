package my.javacraft.echo.standard.client.sync;

import java.io.*;
import java.net.InetSocketAddress;
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

    private static final int CONNECT_TIMEOUT_MILLIS = 1_000;
    private static final int MAX_QUEUED_RESPONSES = 128;
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>(MAX_QUEUED_RESPONSES);

    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter clientWritingStreamToServerSocket;

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
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);
            // Send text commands/messages from client to server.
            this.clientWritingStreamToServerSocket = new PrintWriter(socket.getOutputStream(), true);

            log.info("Sync client '{}' is connected", socket);

            awaitResponseFromServer(threadName);
        } catch (Exception e) {
            close();
            throw new IllegalStateException("Failed to connect to %s:%d".formatted(host, port), e);
        }
    }

    /**
     * Virtual thread here is the background socket listener.
     * <p>
     * It continuously reads lines from the server input stream and enqueues them into responseQueue.
     * <p>
     * Why virtual: It provides the same blocking-style code as a normal thread, but much cheaper to create/schedule
     * than a platform thread.
     */
    private void awaitResponseFromServer(String threadName) throws IOException {
        Reader inputStreamReader = new InputStreamReader(socket.getInputStream());
        BufferedReader clientReadingStreamFromServerSocket = new BufferedReader(inputStreamReader);
        Thread.ofVirtual()
                .name(threadName + "-" + port)
                .start(() -> {
                    boolean serverClosedConnection = false;
                    try {
                        String line;
                        while ((line = clientReadingStreamFromServerSocket.readLine()) != null) {
                            if (!enqueueResponse(line)) {
                                break;
                            }
                        }
                        // EOF means server closed its output stream.
                        if (!closedByClient.get()) {
                            serverClosedConnection = true;
                        }
                    } catch (SocketException e) {
                        // A local close can interrupt readLine(); don't mark it as server-initiated closure.
                        if (!closedByClient.get()) {
                            serverClosedConnection = true;
                            log.warn("Listener socket error: {}", e.getMessage());
                        }
                    } catch (IOException e) {
                        // Read I/O failure while client is still open indicates the remote side is no longer readable.
                        if (!closedByClient.get()) {
                            serverClosedConnection = true;
                            log.warn("Listener error: {}", e.getMessage());
                        }
                    } finally {
                        if (serverClosedConnection) {
                            closedByServer = true;
                        }
                    }
                });
    }

    /**
     * Bounds in-memory buffering of server responses so a noisy peer cannot grow memory unboundedly.
     */
    private boolean enqueueResponse(String line) {
        if (responseQueue.offer(line)) {
            return true;
        }
        log.warn(
                "Response queue overflow (max {}). Closing connection to protect memory.",
                MAX_QUEUED_RESPONSES
        );
        close();
        return false;
    }

    public void sendMessage(String message) {
        if (!isConnected()) {
            throw new IllegalStateException("Client is not connected to %s:%d".formatted(host, port));
        }

        clientWritingStreamToServerSocket.println(message);

        if (clientWritingStreamToServerSocket.checkError()) {
            close();
            throw new IllegalStateException("Failed to send message to %s:%d".formatted(host, port));
        }
    }

    public String readMessage() {
        try {
            return responseQueue.poll(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public boolean isConnected() {
        return !closedByClient.get()
                && !closedByServer
                && socket != null
                && socket.isConnected()
                && !socket.isClosed()
                && clientWritingStreamToServerSocket != null;
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
                userInput = line;

                sendMessage(userInput);

                System.out.println(readMessage());
                if ("bye".equalsIgnoreCase(userInput)) {
                    break;
                }
            }
        } catch (IllegalStateException e) {
            log.warn(
                    "Client loop stopped because connection to: '{}:{}' is not available: {}",
                    host, port, e.getMessage()
            );
        } catch (IOException e) {
            log.warn(
                    "Couldn't get I/O for the connection to: '{}:{}' because: {}",
                    host, port, e.getMessage()
            );
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        if (!closedByClient.compareAndSet(false, true)) {
            return;
        }
        if (clientWritingStreamToServerSocket != null) {
            try {
                clientWritingStreamToServerSocket.close();
            } catch (Exception e) {
                log.error("Couldn't close output stream", e);
            } finally {
                clientWritingStreamToServerSocket = null;
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Couldn't close socket", e);
            } finally {
                socket = null;
            }
        }
    }
}
