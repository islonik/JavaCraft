package my.javacraft.echo.single.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.Set;
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
    void testFlushPendingWritesThrowsIOExceptionFromChannelWrite() {
        WriteFailingSocketChannel channel = new WriteFailingSocketChannel();
        SelectionKey key = new FakeSelectionKey(channel);

        sender.setKey(key, null);
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

    /**
     * Forces flushPendingWrites() down its write-exception path without
     * relying on Mockito to stub final JDK channel methods.
     */
    private static final class WriteFailingSocketChannel extends SocketChannel {
        private WriteFailingSocketChannel() {
            super(SelectorProvider.provider());
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            throw new IOException("write failed");
        }

        @Override
        public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
            throw new IOException("write failed");
        }

        @Override
        public int read(ByteBuffer dst) {
            return 0;
        }

        @Override
        public long read(ByteBuffer[] dsts, int offset, int length) {
            return 0;
        }

        @Override
        public SocketChannel bind(SocketAddress local) {
            return this;
        }

        @Override
        public <T> SocketChannel setOption(SocketOption<T> name, T value) {
            return this;
        }

        @Override
        public SocketChannel shutdownInput() {
            return this;
        }

        @Override
        public SocketChannel shutdownOutput() {
            return this;
        }

        @Override
        public Socket socket() {
            return new Socket();
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public boolean isConnectionPending() {
            return false;
        }

        @Override
        public boolean connect(SocketAddress remote) {
            return true;
        }

        @Override
        public boolean finishConnect() {
            return true;
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return InetSocketAddress.createUnresolved("localhost", 0);
        }

        @Override
        public SocketAddress getLocalAddress() {
            return InetSocketAddress.createUnresolved("localhost", 0);
        }

        @Override
        public <T> T getOption(SocketOption<T> name) {
            return null;
        }

        @Override
        public Set<SocketOption<?>> supportedOptions() {
            return Collections.emptySet();
        }

        @Override
        protected void implCloseSelectableChannel() {
            // no-op
        }

        @Override
        protected void implConfigureBlocking(boolean block) {
            // no-op
        }
    }

    /**
     * Supplies the sender with a minimal selection key so the test can focus
     * on the write failure instead of selector framework setup.
     */
    private static final class FakeSelectionKey extends SelectionKey {
        private final SocketChannel channel;
        private int interestOps = SelectionKey.OP_READ;

        private FakeSelectionKey(SocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public SelectableChannel channel() {
            return channel;
        }

        @Override
        public Selector selector() {
            return null;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void cancel() {
            // no-op
        }

        @Override
        @SuppressWarnings("MagicConstant")
        public int interestOps() {
            return interestOps;
        }

        @Override
        public SelectionKey interestOps(int ops) {
            interestOps = ops;
            return this;
        }

        @Override
        @SuppressWarnings("MagicConstant")
        public int readyOps() {
            return interestOps;
        }
    }
}
