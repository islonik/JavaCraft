package my.javacraft.echo.single.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SingleMessageSenderTest {

    private SingleMessageSender sender;
    private ServerSocket serverSocket;
    private Socket acceptedSocket;
    private SocketChannel clientChannel;
    private Selector selector;

    @BeforeEach
    void setUp() throws IOException {
        sender = new SingleMessageSender();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (acceptedSocket != null && !acceptedSocket.isClosed()) {
            acceptedSocket.close();
        }
        if (selector != null && selector.isOpen()) {
            selector.close();
        }
        if (clientChannel != null && clientChannel.isOpen()) {
            clientChannel.close();
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    /**
     * Sets up an NIO client channel connected to a local server.
     * Returns the SelectionKey registered with OP_READ.
     * The accepted server-side socket is stored in {@link #acceptedSocket}.
     */
    private SelectionKey createConnection() throws IOException {
        serverSocket = new ServerSocket(0);

        clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);
        clientChannel.connect(new InetSocketAddress("localhost", serverSocket.getLocalPort()));

        acceptedSocket = serverSocket.accept();

        while (!clientChannel.finishConnect()) {
            Thread.yield();
        }

        selector = Selector.open();
        return clientChannel.register(selector, SelectionKey.OP_READ);
    }

    @Test
    void testSendWritesDataToChannel() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key);

        sender.send("hello server");

        InputStream in = acceptedSocket.getInputStream();
        byte[] buf = new byte[256];
        int len = in.read(buf);
        String received = new String(buf, 0, len);

        Assertions.assertEquals("hello server", received);
    }

    @Test
    void testSendMultipleMessages() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key);

        sender.send("first");
        sender.send("second");
        sender.send("third");

        InputStream in = acceptedSocket.getInputStream();
        byte[] buf = new byte[256];
        int totalLen = 0;
        String expected = "firstsecondthird";
        while (totalLen < expected.length()) {
            int len = in.read(buf, totalLen, buf.length - totalLen);
            if (len == -1) break;
            totalLen += len;
        }
        Assertions.assertEquals(expected, new String(buf, 0, totalLen));
    }

    @Test
    void testSendResetsInterestOpsToRead() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key);

        sender.send("test");

        // After send(), interestOps should be restored to OP_READ
        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    @Test
    void testSendBlocksUntilKeyIsSet() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> future = executor.submit(() -> sender.send("blocked"));

            // Give time for send() to enter wait()
            Thread.sleep(150);
            Assertions.assertFalse(future.isDone(), "send() should block until setKey() is called");

            // Set the key — this should unblock send()
            SelectionKey key = createConnection();
            sender.setKey(key);

            future.get(2, TimeUnit.SECONDS);
            Assertions.assertTrue(future.isDone());

            // Verify data was written
            InputStream in = acceptedSocket.getInputStream();
            byte[] buf = new byte[256];
            int len = in.read(buf);
            Assertions.assertEquals("blocked", new String(buf, 0, len));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void testSetKeyToNullCausesSendToBlockAgain() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key);

        // Setting key to null should cause next send() to block
        sender.setKey(null);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> future = executor.submit(() -> sender.send("should block"));

            Thread.sleep(150);
            Assertions.assertFalse(future.isDone(), "send() should block after key was set to null");
        } finally {
            executor.shutdownNow();
        }
    }
}
