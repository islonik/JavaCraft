package my.javacraft.echo.single.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * Single-threaded echo server built on top of a selector.
 * <p>
 * The server speaks a simple line-delimited protocol.
 * Each connection keeps its own decode buffer so fragmented TCP reads
 * and coalesced TCP reads are handled deterministically.
 * <p>
 * @author Lipatov Nikita
 * <p>
 * Framing Strategy: Line-delimited framing with - \r\n.
 * <p>
 * Strategy on both sides:
 * <p>
 * 1) sender always appends - \r\n
 * 2) receiver keeps a per-connection raw byte buffer
 * 3) each read appends raw bytes to that buffer
 * 4) receiver extracts only complete frames ending with \r\n and then decodes them as UTF-8
 * 5) any incomplete suffix stays buffered for the next read
 * 6) if multiple frames arrive in one read, they are queued and processed in order
 */
@Slf4j
public class SingleServer implements Runnable {

    static final int BUFFER_SIZE = 2 * 1024;
    private static final int MAX_EMPTY_WRITES = 1024;
    // A CRLF-terminated text frame keeps the protocol simple and readable while
    // still handling empty messages and fragmented/coalesced TCP packets.
    private static final String MESSAGE_DELIMITER = "\r\n";
    private static final byte[] MESSAGE_DELIMITER_BYTES = MESSAGE_DELIMITER.getBytes(StandardCharsets.US_ASCII);

    private final AtomicInteger connections = new AtomicInteger(0);
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final int port;
    private final ByteBuffer buffer;
    private final Map<SocketChannel, ByteArrayOutputStream> requestBuffers = new HashMap<>();
    private final Map<SocketChannel, Deque<String>> pendingRequests = new HashMap<>();
    private volatile Selector selectorRef;

    /**
     * Allocates the shared read buffer once and keeps the rest of the per-socket
     * state in lightweight maps keyed by the channel itself.
     */
    public SingleServer(int port) {
        this.port = port;
        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);

        log.info("Use next command: telnet localhost {}", port);
    }

    /**
     * Owns the full server lifecycle so callers only need to construct the
     * instance and run it on a thread.
     */
    public void run() {
        Selector selector = null;
        ServerSocketChannel server = null;

        try {
            log.info("Starting server...");

            selector = Selector.open();
            selectorRef = selector;
            server = ServerSocketChannel.open();
            server.socket().bind(new InetSocketAddress(port));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);

            log.info("Server ready, now ready to accept connections...");
            loop(selector, server);

        } catch (Exception e) {
            log.error("Server failure", e);
        } finally {
            selectorRef = null;
            try {
                if (selector != null) {
                    selector.close();
                }
                if (server != null) {
                    server.close();
                }
            } catch (Exception e) {
                // server failed
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Wakes the selector because a plain flag change would not unblock
     * selector.select() promptly during shutdown.
     */
    public void stop() {
        running.set(false);
        Selector selector = selectorRef;
        if (selector != null) {
            selector.wakeup();
        }
    }

    private void loop(Selector selector, ServerSocketChannel server) throws IOException {
        while (running.get() && isNotInterrupted()) {
            int num = selector.select();
            if (num == 0) {
                continue;
            }
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                try {
                    if (key.isAcceptable()) {
                        acceptOp(selector, server);
                    } else if (key.isReadable()) {
                        readOp(key);
                    } else if (key.isWritable()) {
                        writeOp(key);
                    }
                } catch (Exception e) {
                    log.error("Failed to process selected key", e);
                    closeKey(key);
                }
            }
        }
    }

    private boolean isNotInterrupted() {
        return !Thread.currentThread().isInterrupted();
    }

    private void acceptOp(Selector selector, ServerSocketChannel server) throws IOException {
        SocketChannel client = server.accept();
        if (client == null) {
            return;
        }

        log.info("New socket has been accepted!");
        connections.incrementAndGet();

        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        requestBuffers.put(client, new ByteArrayOutputStream());
        pendingRequests.put(client, new ArrayDeque<>());
    }

    /**
     * Switches the channel to write mode only after a complete framed request is
     * available. Partial reads remain buffered for the next selector cycle.
     */
    private void readOp(SelectionKey key) {
        log.debug("Data received, going to read them");
        SocketChannel channel = (SocketChannel) key.channel();

        String result = read(channel);
        if (result == null) {
            return;
        }

        if (!result.isEmpty()) {
            key.attach(result);
            key.interestOps(SelectionKey.OP_WRITE);
        } else {
            clearChannelState(channel);
            key.cancel();
        }
    }

    /**
     * Returns the next complete request frame for the channel.
     * <p>
     * The returned value still includes the line delimiter so an empty client message remains distinguishable from EOF.
     */
    String read(SocketChannel channel) {
        String pendingRequest = pollPendingRequest(channel);
        if (pendingRequest != null) {
            return pendingRequest;
        }

        ByteArrayOutputStream requestBuffer = requestBuffers.computeIfAbsent(channel, ignored -> new ByteArrayOutputStream());
        Deque<String> readyRequests = pendingRequests.computeIfAbsent(channel, ignored -> new ArrayDeque<>());

        try {
            while (true) {
                buffer.clear();
                int numRead = channel.read(buffer);
                if (numRead == 0) {
                    return pollPendingRequest(channel);
                }
                if (numRead == -1) {
                    log.debug("Connection closed by: {}", channel.getRemoteAddress());
                    clearChannelState(channel);
                    decrementConnections();
                    channel.close();
                    return "";
                }

                buffer.flip();
                byte[] data = new byte[numRead];
                buffer.get(data);

                requestBuffer.write(data, 0, numRead);
                bufferCompleteRequests(requestBuffer, readyRequests);

                String nextRequest = readyRequests.pollFirst();
                if (nextRequest != null) {
                    return nextRequest;
                }
            }
        } catch (IOException e) {
            log.error("Unable to read from channel", e);
            clearChannelState(channel);
            decrementConnections();
            try {
                channel.close();
            } catch (IOException e1) {
                // nothing to do, channel dead
                log.error(e1.getMessage(), e1);
            }
        }

        return "";
    }

    /**
     * Writes all fully buffered requests that are already ready for the channel.
     * This keeps coalesced requests in order without waiting for another read.
     */
    private void writeOp(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        String request = (String) key.attachment();
        if (request == null) {
            request = pollPendingRequest(channel);
        }
        if (request == null) {
            key.attach(null);
            key.interestOps(SelectionKey.OP_READ);
            return;
        }

        while (request != null) {
            String normalizedRequest = trimTrailingLineDelimiters(request);

            String response;
            boolean close = false;
            if (normalizedRequest.isEmpty()) {
                response = "Please type something.\r\n";
            } else if ("bye".equalsIgnoreCase(normalizedRequest)) {
                response = "Have a good day!\r\n";
                close = true;
            } else if ("stats".equalsIgnoreCase(normalizedRequest)) {
                response = "Simultaneously connected clients: %s\r\n".formatted(connections.get());
            } else {
                response = "Did you say '" + normalizedRequest + "'?\r\n";
            }

            if (close) {
                if (write(channel, response)) {
                    decrementConnections();
                }
                clearChannelState(channel);
                closeKey(key);
                return;
            }

            if (!write(channel, response)) {
                clearChannelState(channel);
                closeKey(key);
                return;
            }

            request = pollPendingRequest(channel);
        }

        key.attach(null);
        key.interestOps(SelectionKey.OP_READ);
    }

    /**
     * Removes only the protocol delimiter so embedded line breaks remain part of
     * the echoed payload.
     */
    private String trimTrailingLineDelimiters(String request) {
        int end = request.length();
        while (end > 0) {
            char current = request.charAt(end - 1);
            if (current != '\r' && current != '\n') {
                break;
            }
            end--;
        }
        return request.substring(0, end);
    }

    /**
     * Extracts complete frames from raw bytes before decoding them. This avoids
     * corrupting UTF-8 characters that arrive split across socket reads.
     */
    private void bufferCompleteRequests(ByteArrayOutputStream requestBuffer, Deque<String> readyRequests) {
        byte[] bufferedBytes = requestBuffer.toByteArray();
        int frameStart = 0;

        for (int index = 0; index <= bufferedBytes.length - MESSAGE_DELIMITER_BYTES.length; index++) {
            if (!matchesDelimiter(bufferedBytes, index)) {
                continue;
            }

            readyRequests.addLast(new String(
                    bufferedBytes,
                    frameStart,
                    index - frameStart,
                    StandardCharsets.UTF_8) + MESSAGE_DELIMITER);
            frameStart = index + MESSAGE_DELIMITER_BYTES.length;
            index = frameStart - 1;
        }

        retainUnreadBytes(requestBuffer, bufferedBytes, frameStart);
    }

    /**
     * Compares raw bytes with the CRLF delimiter so frame detection stays
     * independent from UTF-8 character boundaries.
     */
    private boolean matchesDelimiter(byte[] bufferedBytes, int index) {
        for (int delimiterOffset = 0; delimiterOffset < MESSAGE_DELIMITER_BYTES.length; delimiterOffset++) {
            if (bufferedBytes[index + delimiterOffset] != MESSAGE_DELIMITER_BYTES[delimiterOffset]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes already-decoded frames and keeps only the unfinished byte suffix
     * for the next non-blocking read event.
     */
    private void retainUnreadBytes(ByteArrayOutputStream requestBuffer, byte[] bufferedBytes, int frameStart) {
        if (frameStart == 0) {
            return;
        }

        requestBuffer.reset();
        requestBuffer.write(bufferedBytes, frameStart, bufferedBytes.length - frameStart);
    }

    private String pollPendingRequest(SocketChannel channel) {
        Deque<String> readyRequests = pendingRequests.get(channel);
        if (readyRequests == null) {
            return null;
        }
        return readyRequests.pollFirst();
    }

    private void clearChannelState(SocketChannel channel) {
        requestBuffers.remove(channel);
        pendingRequests.remove(channel);
    }

    /**
     * Completes the whole response before returning so the caller can safely
     * switch the selection key back to read mode.
     */
    boolean write(SocketChannel channel, String content) {
        try {
            ByteBuffer writeBuffer = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));
            int emptyWrites = 0;
            while (writeBuffer.hasRemaining()) {
                int written = channel.write(writeBuffer);
                if (written == 0) {
                    emptyWrites++;
                    if (emptyWrites >= MAX_EMPTY_WRITES) {
                        log.warn("Write made no progress for {} attempts; closing channel", MAX_EMPTY_WRITES);
                        decrementConnections();
                        return false;
                    }
                    Thread.onSpinWait();
                } else {
                    emptyWrites = 0;
                }
            }
            return true;
        } catch (ClosedChannelException cce) {
            decrementConnections();
            log.info("Client terminated connection.");
            return false;
        } catch (IOException e) {
            decrementConnections();
            log.error("Unable to write content", e);
            try {
                channel.close();
            } catch (IOException e1) {
                // dead channel, nothing to do
            }
            return false;
        }
    }

    private void decrementConnections() {
        connections.updateAndGet(value -> value > 0 ? value - 1 : 0);
    }

    private void closeKey(SelectionKey key) {
        SelectableChannel channel = key.channel();
        try {
            if (channel instanceof SocketChannel socketChannel) {
                clearChannelState(socketChannel);
            }
            channel.close();
        } catch (IOException closeError) {
            log.debug("Error closing channel", closeError);
        } finally {
            key.cancel();
        }
    }

}
