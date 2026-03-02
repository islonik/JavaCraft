package my.javacraft.echo.single.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class SingleServer implements Runnable {

    static final int BUFFER_SIZE = 2 * 1024;
    private static final int MAX_EMPTY_WRITES = 1024;

    private final AtomicInteger connections = new AtomicInteger(0);
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final int port;
    private final ByteBuffer buffer;
    private volatile Selector selectorRef;

    public SingleServer(int port) {
        this.port = port;
        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);

        log.info("Use next command: telnet localhost {}", port);
    }

    public void run() {
        Selector selector = null;
        ServerSocketChannel server = null;

        try {
            log.debug("Starting server...");

            selector = Selector.open();
            selectorRef = selector;
            server = ServerSocketChannel.open();
            server.socket().bind(new InetSocketAddress(port));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);

            log.debug("Server ready, now ready to accept connections");
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

    public void stop() {
        running.set(false);
        Selector selector = selectorRef;
        if (selector != null) {
            selector.wakeup();
        }
    }

    private void loop(Selector selector, ServerSocketChannel server) throws IOException {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
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

    private void acceptOp(Selector selector, ServerSocketChannel server) throws IOException {
        SocketChannel client = server.accept();
        if (client == null) {
            return;
        }

        log.info("New socket has been accepted!");
        connections.incrementAndGet();

        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

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
            key.cancel();
        }
    }

    String read(SocketChannel channel) {
        StringBuilder result = null;
        boolean nonBlocking = !channel.isBlocking();

        try {
            while (true) {
                buffer.clear();
                int numRead = channel.read(buffer);
                if (numRead == 0) {
                    if (result == null) {
                        return null;
                    }
                    return result.toString();
                }
                if (numRead == -1) {
                    log.debug("Connection closed by: {}", channel.getRemoteAddress());
                    decrementConnections();
                    channel.close();
                    return "";
                }

                buffer.flip();
                byte[] data = new byte[numRead];
                buffer.get(data);

                if (result == null) {
                    result = new StringBuilder();
                }
                result.append(new String(data, StandardCharsets.UTF_8));

                if (!nonBlocking) {
                    return result.toString();
                }
            }
        } catch (IOException e) {
            log.error("Unable to read from channel", e);
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

    private void writeOp(SelectionKey key) {
        String request = (String) key.attachment();
        if (request == null || request.isEmpty()) {
            key.interestOps(SelectionKey.OP_READ);
            return;
        }

        request = request.replace("\r", "").replace("\n", "");

        String response;
        boolean close = false;
        if (request.isEmpty()) {
            response = "Please type something.\r\n";
        } else if ("bye".equalsIgnoreCase(request)) {
            response = "Have a good day!\r\n";
            close = true;
        } else if ("stats".equalsIgnoreCase(request)) {
            response = "%s simultaneously connected clients.\r\n".formatted(connections.get());
        } else {
            response = "Did you say '" + request + "'?\r\n";
        }

        if (close) {
            if (write((SocketChannel) key.channel(), response)) {
                decrementConnections();
            }
            closeKey(key);
            return;
        }

        if (write((SocketChannel) key.channel(), response)) {
            key.attach(null);
            key.interestOps(SelectionKey.OP_READ);
        } else {
            closeKey(key);
        }
    }

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
        try {
            key.channel().close();
        } catch (IOException closeError) {
            log.debug("Error closing channel", closeError);
        } finally {
            key.cancel();
        }
    }

}
