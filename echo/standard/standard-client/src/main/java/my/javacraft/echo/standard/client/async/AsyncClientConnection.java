package my.javacraft.echo.standard.client.async;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.echo.standard.client.tools.UserClient;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class AsyncClientConnection implements AutoCloseable {

    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>(UserClient.MAX_QUEUED_RESPONSES);

    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter clientWritingStreamToServerSocket;
    @Getter
    private volatile boolean socketClosed = false;

    public AsyncClientConnection(String threadName, String host, int port) {
        this.host = host;
        this.port = port;
        try {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(host, port), UserClient.CONNECT_TIMEOUT_MILLIS);

            Writer outputStreamWriter = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            // Send text commands/messages from client to server.
            this.clientWritingStreamToServerSocket = new PrintWriter(outputStreamWriter, true);

            BufferedReader inStream = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            log.info("Async client '{}' is connected", socket);

            Thread.ofPlatform()
                    .name(threadName + "-" + port)
                    .daemon(true)
                    .start(() -> listen(inStream));
        } catch (IOException e) {
            close();
            throw new IllegalStateException("Failed to connect to %s:%d".formatted(host, port), e);
        }
    }

    private void listen(BufferedReader inStream) {
        try {
            String line;
            while ((line = inStream.readLine()) != null) {
                if (!enqueueResponse(line)) {
                    break;
                }
            }
        } catch (SocketException ignored) {
            // expected when close() is called while blocking on readLine()
        } catch (IOException e) {
            log.warn("Couldn't get I/O for the connection to: {}:{}", host, port, e);
        } finally {
            socketClosed = true;
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
                UserClient.MAX_QUEUED_RESPONSES
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
            return responseQueue.poll(UserClient.CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public boolean isConnected() {
        return !socketClosed
                && socket != null
                && socket.isConnected()
                && !socket.isClosed()
                && clientWritingStreamToServerSocket != null;
    }

    @Override
    public void close() {
        if (clientWritingStreamToServerSocket != null) {
            clientWritingStreamToServerSocket.close();
            clientWritingStreamToServerSocket = null;
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
        socketClosed = true;
    }
}
