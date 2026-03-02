package my.javacraft.echo.single.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SingleMessageListenerTest {

    private ServerSocket serverSocket;

    @BeforeEach
    void setUp() throws IOException {
        serverSocket = new ServerSocket(0);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    // ── newResponse() tests ──────────────────────────────────────────

    @Test
    void testNewResponseReadsMessage() throws Exception {
        SocketChannel client = SocketChannel.open(
                new InetSocketAddress("localhost", serverSocket.getLocalPort()));
        Socket server = serverSocket.accept();

        server.getOutputStream().write("hello world\r\n".getBytes(StandardCharsets.UTF_8));
        server.getOutputStream().flush();
        Thread.sleep(50);

        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());
        String result = listener.newResponse(client);

        Assertions.assertEquals("hello world", result);

        client.close();
        server.close();
    }

    @Test
    void testNewResponsePreservesWhitespaceInsideFrame() throws Exception {
        SocketChannel client = SocketChannel.open(
                new InetSocketAddress("localhost", serverSocket.getLocalPort()));
        Socket server = serverSocket.accept();

        server.getOutputStream().write("  padded message  \r\n".getBytes(StandardCharsets.UTF_8));
        server.getOutputStream().flush();
        Thread.sleep(50);

        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());
        String result = listener.newResponse(client);

        Assertions.assertEquals("  padded message  ", result);

        client.close();
        server.close();
    }

    @Test
    void testNewResponseReturnsNullOnEof() throws Exception {
        SocketChannel client = SocketChannel.open(
                new InetSocketAddress("localhost", serverSocket.getLocalPort()));
        Socket server = serverSocket.accept();
        server.close(); // causes EOF on client side
        Thread.sleep(50);

        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());
        // numRead == -1 → returns null after closing channel
        String result = listener.newResponse(client);

        Assertions.assertNull(result);
        client.close();
    }

    @Test
    void testNewResponseReturnsNullOnClosedChannel() throws Exception {
        SocketChannel client = SocketChannel.open(
                new InetSocketAddress("localhost", serverSocket.getLocalPort()));
        serverSocket.accept(); // complete the handshake
        client.close(); // close before reading

        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());
        String result = listener.newResponse(client);

        Assertions.assertNull(result);
    }

    @Test
    void testNewResponseHandlesLargeMessage() throws Exception {
        SocketChannel client = SocketChannel.open(
                new InetSocketAddress("localhost", serverSocket.getLocalPort()));
        Socket server = serverSocket.accept();

        // Send a message close to BUFFER_SIZE (2048)
        byte[] largePayload = new byte[2000];
        java.util.Arrays.fill(largePayload, (byte) 'A');
        server.getOutputStream().write(largePayload);
        server.getOutputStream().write("\r\n".getBytes(StandardCharsets.UTF_8));
        server.getOutputStream().flush();
        Thread.sleep(100);

        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());
        String result = listener.newResponse(client);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2000, result.length());

        client.close();
        server.close();
    }

    @Test
    void testNewResponseWaitsForCompleteFrameAcrossReads() throws Exception {
        ScriptedSocketChannel channel = new ScriptedSocketChannel(
                new int[] {3, 0, 4, 0},
                new String[] {"hel", "", "lo\r\n", ""});

        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());

        Assertions.assertNull(listener.newResponse(channel),
                "Partial data without the line delimiter must stay buffered");
        Assertions.assertEquals("hello", listener.newResponse(channel));
    }

    @Test
    void testNewResponseSplitsMultipleFramesFromSingleRead() throws Exception {
        ScriptedSocketChannel channel = new ScriptedSocketChannel(
                new int[] {15, 0},
                new String[] {"first\r\nsecond\r\n", ""});

        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());

        Assertions.assertEquals("first", listener.newResponse(channel));
        Assertions.assertEquals("second", listener.newResponse(channel));
        Assertions.assertNull(listener.newResponse(channel));
    }

    // ── run() integration tests ──────────────────────────────────────

    @Test
    void testRunSetsKeyOnSenderAfterConnect() throws Exception {
        SingleNetworkManager mgr = new SingleNetworkManager();
        SingleMessageSender sender = new SingleMessageSender();
        mgr.setSingleMessageSender(sender);

        SingleMessageListener listener = new SingleMessageListener(mgr);
        ExecutorService listenerExec = Executors.newSingleThreadExecutor();
        try {
            listenerExec.execute(listener);

            mgr.openSocket("localhost", serverSocket.getLocalPort());
            Socket serverSide = serverSocket.accept();

            // Wait for OP_CONNECT to be processed
            Thread.sleep(300);

            // Sender should have a key — verify by checking send() completes without blocking
            ExecutorService sendExec = Executors.newSingleThreadExecutor();
            Future<?> sendFuture = sendExec.submit(() -> sender.send("ping"));
            sendFuture.get(2, TimeUnit.SECONDS);
            sendExec.shutdownNow();

            serverSide.close();
        } finally {
            mgr.closeSocket();
            listenerExec.shutdownNow();
        }
    }

    @Test
    void testRunDoesNotQueueNullResponse() throws Exception {
        SingleNetworkManager mgr = new SingleNetworkManager();
        SingleMessageSender sender = new SingleMessageSender();
        mgr.setSingleMessageSender(sender);

        SingleMessageListener listener = new SingleMessageListener(mgr);
        ExecutorService listenerExec = Executors.newSingleThreadExecutor();
        try {
            listenerExec.execute(listener);

            mgr.openSocket("localhost", serverSocket.getLocalPort());
            Socket serverSide = serverSocket.accept();

            // Wait for OP_CONNECT
            Thread.sleep(300);

            // Send a message, then close the server side to cause EOF on next read
            sender.send("ping");
            Thread.sleep(100);
            byte[] buf = new byte[256];
            int len = serverSide.getInputStream().read(buf);
            Assertions.assertEquals("ping\r\n", new String(buf, 0, len));

            serverSide.getOutputStream().write("pong\r\n".getBytes(StandardCharsets.UTF_8));
            serverSide.getOutputStream().flush();
            serverSide.close(); // causes EOF after "pong" is read
            Thread.sleep(500);

            // "pong" should be queued
            String queued = mgr.getMessage();
            Assertions.assertEquals("pong", queued);

            // The null from EOF should NOT be queued → queue should be empty
            String afterEof = mgr.getMessage();
            Assertions.assertNull(afterEof);
        } finally {
            mgr.closeSocket();
            listenerExec.shutdownNow();
        }
    }

    @Test
    void testRunClosesSocketAndResetsKeyOnConnectionFailure() throws Exception {
        // Get a free port with nothing listening on it
        int deadPort;
        try (ServerSocket temp = new ServerSocket(0)) {
            deadPort = temp.getLocalPort();
        }

        SingleNetworkManager mgr = new SingleNetworkManager();
        SingleMessageSender sender = new SingleMessageSender();
        mgr.setSingleMessageSender(sender);

        SingleMessageListener listener = new SingleMessageListener(mgr);
        ExecutorService listenerExec = Executors.newSingleThreadExecutor();
        try {
            listenerExec.execute(listener);

            // Connect to dead port → finishConnect() throws ConnectException (IOException)
            mgr.openSocket("localhost", deadPort);
            Thread.sleep(500);

            // After IOException catch, setKey(null) is called → send should block
            ExecutorService sendExec = Executors.newSingleThreadExecutor();
            try {
                Future<?> future = sendExec.submit(() -> sender.send("after-error"));
                Thread.sleep(200);
                Assertions.assertFalse(future.isDone(),
                        "send() should block because key was reset to null after connection failure");
            } finally {
                sendExec.shutdownNow();
            }
        } finally {
            mgr.closeSocket();
            listenerExec.shutdownNow();
        }
    }

    @Test
    void testRunQueuesReceivedMessages() throws Exception {
        SingleNetworkManager mgr = new SingleNetworkManager();
        SingleMessageSender sender = new SingleMessageSender();
        mgr.setSingleMessageSender(sender);

        SingleMessageListener listener = new SingleMessageListener(mgr);
        ExecutorService listenerExec = Executors.newSingleThreadExecutor();
        try {
            listenerExec.execute(listener);

            mgr.openSocket("localhost", serverSocket.getLocalPort());
            Socket serverSide = serverSocket.accept();

            // Wait for OP_CONNECT
            Thread.sleep(300);

            // Client sends message to server
            sender.send("hello");
            Thread.sleep(100);

            // Server reads and verifies
            byte[] buf = new byte[256];
            int len = serverSide.getInputStream().read(buf);
            Assertions.assertEquals("hello\r\n", new String(buf, 0, len));

            // Server sends response back
            serverSide.getOutputStream().write("echo: hello\r\n".getBytes(StandardCharsets.UTF_8));
            serverSide.getOutputStream().flush();

            // Wait for OP_READ to be processed
            Thread.sleep(300);

            String queued = mgr.getMessage();
            Assertions.assertEquals("echo: hello", queued);

            serverSide.close();
        } finally {
            mgr.closeSocket();
            listenerExec.shutdownNow();
        }
    }

    @Test
    void testRunDrainsMultipleResponsesFromSingleReadEvent() throws Exception {
        SingleNetworkManager mgr = new SingleNetworkManager();
        SingleMessageSender sender = new SingleMessageSender();
        mgr.setSingleMessageSender(sender);

        SingleMessageListener listener = new SingleMessageListener(mgr);
        ExecutorService listenerExec = Executors.newSingleThreadExecutor();
        try {
            listenerExec.execute(listener);

            mgr.openSocket("localhost", serverSocket.getLocalPort());
            Socket serverSide = serverSocket.accept();

            Thread.sleep(300);

            sender.send("ping");
            Thread.sleep(100);
            byte[] buf = new byte[256];
            int len = serverSide.getInputStream().read(buf);
            Assertions.assertEquals("ping\r\n", new String(buf, 0, len));

            serverSide.getOutputStream().write("reply1\r\nreply2\r\n".getBytes(StandardCharsets.UTF_8));
            serverSide.getOutputStream().flush();
            Thread.sleep(300);

            Assertions.assertEquals("reply1", mgr.getMessage());
            Assertions.assertEquals("reply2", mgr.getMessage());
        } finally {
            mgr.closeSocket();
            listenerExec.shutdownNow();
        }
    }

    @Test
    void testBufferSizeConstant() {
        Assertions.assertEquals(2048, SingleMessageListener.BUFFER_SIZE);
    }

    // ── Mockito-based tests for defensive catch blocks ────────────────

    @Test
    void testNewResponseHandlesCloseFailureAfterReadIOException() throws Exception {
        // Covers newResponse() inner catch(IOException) on channel.close() L86-88
        SocketChannel mockChannel = mock(SocketChannel.class);
        when(mockChannel.read(any(ByteBuffer.class))).thenThrow(new IOException("read failed"));
        doThrow(new IOException("close also failed")).when(mockChannel).close();

        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());
        String result = listener.newResponse(mockChannel);

        Assertions.assertNull(result);
    }

    @Test
    void testNewResponseClosesChannelOnReadIOException() throws Exception {
        // Covers newResponse() catch(IOException) L82-89 — verifies close() is called
        SocketChannel mockChannel = mock(SocketChannel.class);
        when(mockChannel.read(any(ByteBuffer.class))).thenThrow(new IOException("read failed"));

        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());
        String result = listener.newResponse(mockChannel);

        Assertions.assertNull(result);
        verify(mockChannel).close();
    }

    @Test
    void testRunQueuesMultipleMessages() throws Exception {
        SingleNetworkManager mgr = new SingleNetworkManager();
        SingleMessageSender sender = new SingleMessageSender();
        mgr.setSingleMessageSender(sender);

        SingleMessageListener listener = new SingleMessageListener(mgr);
        ExecutorService listenerExec = Executors.newSingleThreadExecutor();
        try {
            listenerExec.execute(listener);

            mgr.openSocket("localhost", serverSocket.getLocalPort());
            Socket serverSide = serverSocket.accept();

            // Wait for OP_CONNECT
            Thread.sleep(300);

            // First round-trip
            sender.send("msg1");
            Thread.sleep(100);
            byte[] buf = new byte[256];
            int len = serverSide.getInputStream().read(buf);
            Assertions.assertEquals("msg1\r\n", new String(buf, 0, len));

            serverSide.getOutputStream().write("reply1\r\n".getBytes(StandardCharsets.UTF_8));
            serverSide.getOutputStream().flush();
            Thread.sleep(300);

            Assertions.assertEquals("reply1", mgr.getMessage());

            // Second round-trip
            sender.send("msg2");
            Thread.sleep(100);
            len = serverSide.getInputStream().read(buf);
            Assertions.assertEquals("msg2\r\n", new String(buf, 0, len));

            serverSide.getOutputStream().write("reply2\r\n".getBytes(StandardCharsets.UTF_8));
            serverSide.getOutputStream().flush();
            Thread.sleep(300);

            Assertions.assertEquals("reply2", mgr.getMessage());

            serverSide.close();
        } finally {
            mgr.closeSocket();
            listenerExec.shutdownNow();
        }
    }

    /**
     * Feeds exact read chunks to the listener so framing behavior can be
     * verified without timing-sensitive socket scheduling.
     */
    private static final class ScriptedSocketChannel extends SocketChannel {
        private final int[] reads;
        private final byte[][] payloads;
        private int readIndex;
        private boolean open = true;

        private ScriptedSocketChannel(int[] reads, String[] payloads) {
            super(SelectorProvider.provider());
            this.reads = reads.clone();
            this.payloads = new byte[payloads.length][];
            for (int i = 0; i < payloads.length; i++) {
                this.payloads[i] = payloads[i].getBytes(StandardCharsets.UTF_8);
            }
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            if (!open) {
                throw new IOException("channel closed");
            }
            if (readIndex >= reads.length) {
                return 0;
            }

            int next = reads[readIndex];
            if (next <= 0) {
                readIndex++;
                return next;
            }

            byte[] source = payloads[readIndex];
            int toCopy = Math.min(next, Math.min(source.length, dst.remaining()));
            dst.put(source, 0, toCopy);
            readIndex++;
            return toCopy;
        }

        @Override
        public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
            long total = 0;
            for (int i = offset; i < offset + length; i++) {
                int read = read(dsts[i]);
                if (read <= 0) {
                    return total == 0 ? read : total;
                }
                total += read;
            }
            return total;
        }

        @Override
        public int write(ByteBuffer src) {
            int written = src.remaining();
            src.position(src.limit());
            return written;
        }

        @Override
        public long write(ByteBuffer[] srcs, int offset, int length) {
            long total = 0;
            for (int i = offset; i < offset + length; i++) {
                total += write(srcs[i]);
            }
            return total;
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
            return InetSocketAddress.createUnresolved("localhost", 8077);
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
            open = false;
        }

        @Override
        protected void implConfigureBlocking(boolean block) {
            // no-op
        }
    }
}
