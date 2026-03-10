package my.javacraft.echo.standard.client.async;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.echo.standard.client.tools.UserClient;

/**
 * StandardAsyncClient.
 * <p>
 * @author Lipatov Nikita
 */
@Slf4j
public class StandardAsyncClient extends UserClient implements Runnable, AutoCloseable {

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
    // Client sends "bye" -> server responds -> server closes its side -> listener detects EOF -> closedByServer = true
    @Getter
    private volatile boolean closedByServer = false;

    public StandardAsyncClient(
            final String threadName,
            final String host,
            final int port) {

        super(host, port);

        this.host = host;
        this.port = port;

        try {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);

            Writer outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            // Send text commands/messages from client to server.
            this.clientWritingStreamToServerSocket = new PrintWriter(outputStreamWriter, true);

            log.info("Async client '{}' is connected", socket);

            awaitResponseFromServer(threadName);
        } catch (Exception e) {
            close();
            throw new IllegalStateException("Failed to connect to %s:%d".formatted(host, port), e);
        }
    }

    private void awaitResponseFromServer(String threadName) throws IOException {
        Reader inputStreamReader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader clientReadingStreamFromServerSocket = new BufferedReader(inputStreamReader);
        Thread.ofPlatform()
                .name(threadName + "-" + port)
                .daemon(true)
                .start(() -> listen(clientReadingStreamFromServerSocket));
    }

    private void listen(BufferedReader clientReadingStreamFromServerSocket) {
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

    @Override
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

    @Override
    public String readMessage() {
        try {
            return responseQueue.poll(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
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
        readUserMessages(log);
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
