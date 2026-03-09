package my.javacraft.echo.standard.client.async;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class AsyncClientConnection implements AutoCloseable {

    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter outStream;
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    @Getter
    private volatile boolean socketClosed = false;

    public AsyncClientConnection(String threadName, String host, int port) {
        this.host = host;
        this.port = port;
        try {
            this.socket = new Socket(host, port);
            this.outStream = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            log.info("Async client {} is connected", socket);

            Thread.ofPlatform()
                    .name(threadName + "-" + port)
                    .daemon(true)
                    .start(() -> listen(inStream));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void listen(BufferedReader inStream) {
        try {
            String line;
            while ((line = inStream.readLine()) != null) {
                responseQueue.add(line);
            }
        } catch (SocketException ignored) {
            // expected when close() is called while blocking on readLine()
        } catch (IOException e) {
            log.warn("Couldn't get I/O for the connection to: {}:{}", host, port);
        } finally {
            socketClosed = true;
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
