package my.javacraft.echo.single.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

import static java.nio.channels.SelectionKey.*;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class SingleServer implements Runnable {

    private static final AtomicInteger connections = new AtomicInteger(0);

    private final int port;
    private final ByteBuffer buffer;

    public SingleServer(int port) {
        this.port = port;
        this.buffer = ByteBuffer.allocate(2 * 1024);

        log.info("Use next command: telnet localhost " + port);
    }

    public void run() {
        Selector selector = null;
        ServerSocketChannel server = null;

        try {
            log.debug("Starting server...");

            selector = Selector.open();
            server = ServerSocketChannel.open();
            server.socket().bind(new InetSocketAddress(port));
            server.configureBlocking(false);
            server.register(selector, OP_ACCEPT);

            log.debug("Server ready, now ready to accept connections");
            loop(selector, server);

        } catch (Throwable e) {
            log.error("Server failure", e);
        } finally {
            try {
                if (selector != null) {
                    selector.close();
                }
                if (server != null) {
                    server.socket().close();
                    server.close();
                }
            } catch (Exception e) {
                // server failed
                log.error(e.getMessage(), e);
            }
        }
    }

    private void loop(Selector selector, ServerSocketChannel server) throws IOException {
        while (true) {
            int num = selector.select();
            if (num == 0) {
                continue;
            }
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()){
                SelectionKey key = keys.next();
                keys.remove();

                if (key.isConnectable()){
                    log.debug("Connectable detected");
                    try (SocketChannel socketChannel = (SocketChannel)key.channel()) {
                        socketChannel.finishConnect();
                    }
                } else if (key.isAcceptable()){
                    acceptOp(selector, server);
                } else if (key.isReadable()){
                    readOp(key);
                } else if (key.isWritable()){
                    writeOp(key);
                }
            }
        }
    }

    private void acceptOp(Selector selector, ServerSocketChannel server) throws IOException {
        SocketChannel client = server.accept();

        log.info("New socket has been accepted!");
        connections.incrementAndGet();

        client.configureBlocking(false);
        client.register(selector, OP_READ);
    }

    private void readOp(SelectionKey key) {
        log.debug("Data received, going to read them");
        SocketChannel channel = (SocketChannel) key.channel();

        String result = read(channel);

        if (result != null && !result.isEmpty()) {
            key.attach(result);
            key.interestOps(OP_WRITE);
        }
    }

    String read(SocketChannel channel) {
        buffer.clear();

        try {
            int numRead = channel.read(buffer);

            if (numRead == -1){
                log.debug("Connection closed by: {}", channel.getRemoteAddress());
                channel.close();
                return "";
            }

            byte[] data = new byte[numRead];
            System.arraycopy(buffer.array(), 0, data, 0, numRead);

            // return request;
            String result = new String(data, StandardCharsets.UTF_8);
            log.debug("Got [{}] from [{}]", result, channel.getRemoteAddress());
            return result;
        } catch (IOException e) {
            log.error("Unable to read from channel", e);
            connections.decrementAndGet();
            try {
                channel.close();
            } catch (IOException e1) {
                // nothing to do, channel dead
                log.error(e.getMessage(), e);
            }
        }

        return "";
    }

    private void writeOp(SelectionKey key) throws IOException {
        String request = (String)key.attachment();
        if (request == null || request.isEmpty()) {
            return;
        }

        request = request.replace("\r\n", "");

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
            write((SocketChannel) key.channel(), response);
            key.channel().close();
            key.cancel();
        }

        if (write((SocketChannel) key.channel(), response)){
            key.interestOps(OP_READ);
        } else {
            key.channel().close();
            key.cancel();
        }
    }

    boolean write(SocketChannel channel, String content){
        try {
            channel.write(ByteBuffer.wrap(content.getBytes()));
            return true;
        } catch (ClosedChannelException cce) {
            connections.decrementAndGet();
            log.info("Client terminated connection.");
            return false;
        } catch (IOException e) {
            connections.decrementAndGet();
            log.error("Unable to write content", e);
            try {
                channel.close();
            } catch (IOException e1) {
                //dead channel, nothing to do
            }
            return false;
        }
    }

}
