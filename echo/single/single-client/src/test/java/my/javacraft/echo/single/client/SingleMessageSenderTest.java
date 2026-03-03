package my.javacraft.echo.single.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleMessageSenderTest {

    private SingleMessageSender sender;
    private ServerSocket serverSocket;
    private Socket acceptedSocket;
    private SocketChannel clientChannel;
    private Selector selector;

    @BeforeEach
    void setUp() {
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

    /**
     * The sender now queues outbound data first and relies on the selector
     * thread to flush it later. Tests call flushPendingWrites() directly so the
     * old socket assertions can still verify the bytes that would be written.
     */
    private void flushQueuedWrites() throws IOException {
        sender.flushPendingWrites();
    }

    @Test
    void testSendWritesDataToChannel() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key, selector);

        sender.send("hello server");
        flushQueuedWrites();

        InputStream in = acceptedSocket.getInputStream();
        byte[] buf = new byte[256];
        int len = in.read(buf);
        String received = new String(buf, 0, len);

        Assertions.assertEquals("hello server\r\n", received);
    }

    @Test
    void testSendMultipleMessages() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key, selector);

        sender.send("first");
        sender.send("second");
        sender.send("third");
        flushQueuedWrites();

        InputStream in = acceptedSocket.getInputStream();
        byte[] buf = new byte[256];
        int totalLen = 0;
        String expected = "first\r\nsecond\r\nthird\r\n";
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
        sender.setKey(key, selector);

        sender.send("test");
        Assertions.assertEquals(SelectionKey.OP_READ | SelectionKey.OP_WRITE, key.interestOps());
        flushQueuedWrites();

        // After the queued write is flushed, the key should return to read mode.
        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    @Test
    void testSendEmptyString() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key, selector);

        // Empty input still becomes a framed CRLF message and is flushed later.
        sender.send("");
        Assertions.assertEquals(SelectionKey.OP_READ | SelectionKey.OP_WRITE, key.interestOps());
        flushQueuedWrites();

        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    @Test
    void testSendPreservesExistingCrLfDelimiter() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key, selector);

        sender.send("already framed\r\n");
        flushQueuedWrites();

        InputStream in = acceptedSocket.getInputStream();
        byte[] buf = new byte[256];
        int len = in.read(buf);

        Assertions.assertEquals("already framed\r\n", new String(buf, 0, len));
    }

    @Test
    void testSendNormalizesTrailingLfToCrLf() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key, selector);

        sender.send("unix newline\n");
        flushQueuedWrites();

        InputStream in = acceptedSocket.getInputStream();
        byte[] buf = new byte[256];
        int len = in.read(buf);

        Assertions.assertEquals("unix newline\r\n", new String(buf, 0, len));
    }

    @Test
    void testSendNormalizesTrailingCrToCrLf() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key, selector);

        sender.send("carriage return only\r");
        flushQueuedWrites();

        InputStream in = acceptedSocket.getInputStream();
        byte[] buf = new byte[256];
        int len = in.read(buf);

        Assertions.assertEquals("carriage return only\r\n", new String(buf, 0, len));
    }

    @Test
    void testSendLargeMessage() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key, selector);

        // Send a large message — exercises the write loop's hasRemaining() check
        String largeMessage = "X".repeat(5000);
        sender.send(largeMessage);
        flushQueuedWrites();

        InputStream in = acceptedSocket.getInputStream();
        byte[] buf = new byte[8192];
        int totalLen = 0;
        while (totalLen < largeMessage.length() + 2) {
            int len = in.read(buf, totalLen, buf.length - totalLen);
            if (len == -1) break;
            totalLen += len;
        }
        Assertions.assertEquals(largeMessage + "\r\n", new String(buf, 0, totalLen));
    }

    @Test
    void testSendBlocksUntilKeyIsSet() throws Exception {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> future = executor.submit(() -> sender.send("blocked"));

            // Give time for send() to enter wait()
            Thread.sleep(150);
            Assertions.assertFalse(future.isDone(), "send() should block until setKey() is called");

            // Set the key — this should unblock send()
            SelectionKey key = createConnection();
            sender.setKey(key, selector);

            future.get(2, TimeUnit.SECONDS);
            Assertions.assertTrue(future.isDone());
            flushQueuedWrites();

            // Verify the queued bytes are written once the selector flushes them.
            InputStream in = acceptedSocket.getInputStream();
            byte[] buf = new byte[256];
            int len = in.read(buf);
            Assertions.assertEquals("blocked\r\n", new String(buf, 0, len));
        }
    }

    @Test
    void testSendHandlesCancelledKey() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key, selector);

        // Close the channel → key becomes cancelled
        clientChannel.close();

        // send() calls key.interestOps() which throws CancelledKeyException — now caught gracefully
        Assertions.assertDoesNotThrow(() -> sender.send("after close"));
    }

    @Test
    void testSendHandlesInterruptDuringWait() {
        // key is null by default → send() will enter wait()
        // With interrupt flag set, wait() immediately throws InterruptedException
        Thread.currentThread().interrupt();

        Assertions.assertDoesNotThrow(() -> sender.send("interrupted"));

        // send() catches InterruptedException and re-sets the interrupt flag
        Assertions.assertTrue(Thread.interrupted());
    }

    @Test
    void testSetKeyToNullCausesSendToBlockAgain() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key, selector);

        // Setting key to null should cause next send() to block
        sender.setKey(null, null);

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> future = executor.submit(() -> sender.send("should block"));

            Thread.sleep(150);
            Assertions.assertFalse(future.isDone(), "send() should block after key was set to null");
        }
    }

    @Test
    @SuppressWarnings("MagicConstant")
    void testFlushPendingWritesThrowsIOExceptionFromChannelWrite() throws Exception {
        SocketChannel mockChannel = mock(SocketChannel.class);
        when(mockChannel.write(any(ByteBuffer.class))).thenThrow(new IOException("write failed"));

        SelectionKey mockKey = mock(SelectionKey.class);
        when(mockKey.channel()).thenReturn(mockChannel);
        when(mockKey.interestOps()).thenReturn(SelectionKey.OP_READ);
        when(mockKey.interestOps(anyInt())).thenReturn(mockKey);

        sender.setKey(mockKey, null);
        sender.send("test");

        Assertions.assertThrows(IOException.class, () -> sender.flushPendingWrites());
    }

    @Test
    void testFlushPendingWritesHandlesCancelledKeyDuringRestore() throws Exception {
        SelectionKey key = createConnection();
        sender.setKey(key, selector);

        sender.send("after flush");
        clientChannel.close();

        Assertions.assertThrows(IOException.class, () -> sender.flushPendingWrites());
    }
}
