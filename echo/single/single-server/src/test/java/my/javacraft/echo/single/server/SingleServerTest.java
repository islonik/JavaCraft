package my.javacraft.echo.single.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SingleServerTest {

    private static final int PORT = 19076;
    private static ExecutorService executorService;

    @BeforeAll
    static void startServer() throws InterruptedException {
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new SingleServer(PORT));
        // Give the server time to bind
        Thread.sleep(500);
    }

    @AfterAll
    static void stopServer() {
        executorService.shutdownNow();
    }

    private String sendAndReceive(Socket socket, String message)
            throws IOException, InterruptedException {
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        out.write(frameClientMessage(message).getBytes());
        out.flush();
        Thread.sleep(200);

        byte[] buf = new byte[1024];
        int len = in.read(buf);
        return new String(buf, 0, len).trim();
    }

    private String frameClientMessage(String message) {
        if (message.endsWith("\r\n")) {
            return message;
        }
        return message + "\r\n";
    }

    @Test
    void testServerAcceptsConnection() throws IOException {
        try (Socket socket = new Socket("localhost", PORT)) {
            Assertions.assertTrue(socket.isConnected());
        }
    }

    @Test
    void testServerAcceptsMultipleConnections() throws IOException {
        try (Socket s1 = new Socket("localhost", PORT);
             Socket s2 = new Socket("localhost", PORT);
             Socket s3 = new Socket("localhost", PORT)) {
            Assertions.assertTrue(s1.isConnected());
            Assertions.assertTrue(s2.isConnected());
            Assertions.assertTrue(s3.isConnected());
        }
    }

    @Test
    void testServerEchosMessage() throws IOException, InterruptedException {
        try (Socket socket = new Socket("localhost", PORT)) {
            socket.setSoTimeout(2000);

            String response = sendAndReceive(socket, "hello world");
            Assertions.assertEquals("Did you say 'hello world'?", response);
        }
    }

    @Test
    void testServerRespondsToBye() throws IOException, InterruptedException {
        try (Socket socket = new Socket("localhost", PORT)) {
            socket.setSoTimeout(2000);

            String response = sendAndReceive(socket, "bye");
            Assertions.assertEquals("Have a good day!", response);
        }
    }

    @Test
    void testServerRespondsToStats() throws IOException, InterruptedException {
        try (Socket socket = new Socket("localhost", PORT)) {
            socket.setSoTimeout(2000);

            String response = sendAndReceive(socket, "stats");
            Assertions.assertTrue(response.endsWith("Simultaneously connected clients: 1"));
        }
    }

    @Test
    void testServerRespondsToCrLfOnly() throws IOException, InterruptedException {
        try (Socket socket = new Socket("localhost", PORT)) {
            socket.setSoTimeout(2000);

            String response = sendAndReceive(socket, "\r\n");
            Assertions.assertEquals("Please type something.", response);
        }
    }

    @Test
    void testServerHandlesMultipleMessagesOnSameConnection() throws IOException, InterruptedException {
        try (Socket socket = new Socket("localhost", PORT)) {
            socket.setSoTimeout(2000);

            String r1 = sendAndReceive(socket, "first");
            Assertions.assertEquals("Did you say 'first'?", r1);

            String r2 = sendAndReceive(socket, "second");
            Assertions.assertEquals("Did you say 'second'?", r2);
        }
    }

    // ── Direct unit tests for read() ─────────────────────────────────

    @Test
    void testReadReturnsContent() {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel(new int[] {7}, new String[] {"hello\r\n"});

        String result = server.read(channel);

        Assertions.assertEquals("hello\r\n", result);
    }

    @Test
    void testReadReturnsEmptyOnEof() throws Exception {
        SingleServer server = new SingleServer(0);
        try (ServerSocket ss = new ServerSocket(0)) {
            SocketChannel client = SocketChannel.open(
                    new InetSocketAddress("localhost", ss.getLocalPort()));
            Socket serverSide = ss.accept();
            serverSide.close(); // causes EOF on client side
            Thread.sleep(50);

            String result = server.read(client);
            Assertions.assertEquals("", result);

            client.close();
        }
    }

    @Test
    void testReadReturnsEmptyOnClosedChannel() throws Exception {
        SingleServer server = new SingleServer(0);
        try (ServerSocket ss = new ServerSocket(0)) {
            SocketChannel client = SocketChannel.open(
                    new InetSocketAddress("localhost", ss.getLocalPort()));
            ss.accept();
            client.close(); // close before read → IOException

            String result = server.read(client);
            Assertions.assertEquals("", result);
        }
    }

    // ── Direct unit tests for write() ────────────────────────────────

    @Test
    void testWriteReturnsTrueOnSuccess() throws Exception {
        SingleServer server = new SingleServer(0);
        try (ServerSocket ss = new ServerSocket(0)) {
            SocketChannel client = SocketChannel.open(
                    new InetSocketAddress("localhost", ss.getLocalPort()));
            Socket serverSide = ss.accept();

            boolean result = server.write(client, "hello");
            Assertions.assertTrue(result);

            byte[] buf = new byte[256];
            int len = serverSide.getInputStream().read(buf);
            Assertions.assertEquals("hello", new String(buf, 0, len));

            client.close();
            serverSide.close();
        }
    }

    @Test
    void testWriteReturnsFalseOnClosedChannel() throws Exception {
        SingleServer server = new SingleServer(0);
        try (ServerSocket ss = new ServerSocket(0)) {
            SocketChannel client = SocketChannel.open(
                    new InetSocketAddress("localhost", ss.getLocalPort()));
            ss.accept();
            client.close(); // close before write

            boolean result = server.write(client, "hello");
            Assertions.assertFalse(result);
        }
    }

    @Test
    void testBufferSizeConstant() {
        Assertions.assertEquals(2048, SingleServer.BUFFER_SIZE);
    }

    @Test
    void testReadReturnsNullWhenNoDataAvailable() {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel(new int[] {0}, new String[] {""});

        String result = server.read(channel);

        Assertions.assertNull(result, "Non-blocking read with 0 bytes should not be treated as disconnect");
    }

    @Test
    void testReadOpAggregatesChunksAvailableInSameReadCycle() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel(
                new int[] {3, 4, 0},
                new String[] {"hel", "lo\r\n"});
        channel.configureBlocking(false);
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);

        Method readOp = SingleServer.class.getDeclaredMethod("readOp", SelectionKey.class);
        readOp.setAccessible(true);

        readOp.invoke(server, key);
        Assertions.assertEquals(SelectionKey.OP_WRITE, key.interestOps(),
                "Complete payload should switch key to write mode");
        Assertions.assertEquals("hello\r\n", key.attachment());
    }

    @Test
    void testReadOpWaitsForCompleteFrameAcrossReadEvents() throws Exception {
        SingleServer server = new SingleServer(0);
        RecordingScriptedSocketChannel channel = new RecordingScriptedSocketChannel(
                new int[] {3, 0, 4, 0},
                new String[] {"hel", "", "lo\r\n", ""});
        channel.configureBlocking(false);
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);

        Method readOp = SingleServer.class.getDeclaredMethod("readOp", SelectionKey.class);
        readOp.setAccessible(true);

        readOp.invoke(server, key);
        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps(),
                "Partial data without the line delimiter must stay buffered");

        readOp.invoke(server, key);
        Assertions.assertEquals(SelectionKey.OP_WRITE, key.interestOps());

        Method writeOp = SingleServer.class.getDeclaredMethod("writeOp", SelectionKey.class);
        writeOp.setAccessible(true);
        writeOp.invoke(server, key);

        Assertions.assertEquals("Did you say 'hello'?\r\n", channel.writtenText());
    }

    @Test
    void testReadReturnsBufferedPendingRequestBeforeReadingAgain() {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel(
                new int[] {15, 0},
                new String[] {"first\r\nsecond\r\n", ""});

        String first = server.read(channel);
        String second = server.read(channel);

        Assertions.assertEquals("first\r\n", first);
        Assertions.assertEquals("second\r\n", second);
    }

    @Test
    void testWriteOpRespondsToEachFrameBufferedFromSingleRead() throws Exception {
        SingleServer server = new SingleServer(0);
        RecordingScriptedSocketChannel channel = new RecordingScriptedSocketChannel(
                new int[] {15, 0},
                new String[] {"first\r\nsecond\r\n", ""});
        channel.configureBlocking(false);
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);

        Method readOp = SingleServer.class.getDeclaredMethod("readOp", SelectionKey.class);
        readOp.setAccessible(true);
        readOp.invoke(server, key);

        Method writeOp = SingleServer.class.getDeclaredMethod("writeOp", SelectionKey.class);
        writeOp.setAccessible(true);
        writeOp.invoke(server, key);

        Assertions.assertEquals(
                "Did you say 'first'?\r\nDid you say 'second'?\r\n",
                channel.writtenText());
        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testWriteOpPullsBufferedPendingRequestWhenAttachmentMissing() throws Exception {
        SingleServer server = new SingleServer(0);
        RecordingScriptedSocketChannel channel = new RecordingScriptedSocketChannel(
                new int[] {0},
                new String[] {""});
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_WRITE);
        key.attach(null);

        var pendingRequestsField = SingleServer.class.getDeclaredField("pendingRequests");
        pendingRequestsField.setAccessible(true);
        var pendingRequests = (java.util.Map<SocketChannel, java.util.Deque<String>>) pendingRequestsField.get(server);
        pendingRequests.put(channel, new java.util.ArrayDeque<>(java.util.List.of("stats\r\n")));

        Method writeOp = SingleServer.class.getDeclaredMethod("writeOp", SelectionKey.class);
        writeOp.setAccessible(true);
        writeOp.invoke(server, key);

        Assertions.assertEquals("Simultaneously connected clients: 0\r\n", channel.writtenText());
        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    @Test
    void testAcceptOpIgnoresNullClient() throws Exception {
        SingleServer server = new SingleServer(0);
        try (Selector selector = Selector.open()) {
            NullAcceptServerSocketChannel serverChannel = new NullAcceptServerSocketChannel();

            Method acceptOp = SingleServer.class.getDeclaredMethod("acceptOp", Selector.class, ServerSocketChannel.class);
            acceptOp.setAccessible(true);
            acceptOp.invoke(server, selector, serverChannel);

            Assertions.assertEquals(0, getConnections(server));
        }
    }

    @Test
    void testWriteReturnsFalseWhenChannelMakesNoProgress() {
        SingleServer server = new SingleServer(0);
        try (ScriptedSocketChannel channel = ScriptedSocketChannel.alwaysZeroWrites()) {
            boolean result = Assertions.assertTimeoutPreemptively(
                    Duration.ofMillis(300),
                    () -> server.write(channel, "hello"));

            Assertions.assertFalse(result, "Write should fail if the channel never accepts bytes");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testStopWithNullSelectorRef() {
        SingleServer server = new SingleServer(0);
        Assertions.assertDoesNotThrow(server::stop);
    }

    @Test
    void testStopWakesUpSelectorWhenPresent() throws Exception {
        SingleServer server = new SingleServer(0);
        WakeupSelector selector = new WakeupSelector();

        var selectorRef = SingleServer.class.getDeclaredField("selectorRef");
        selectorRef.setAccessible(true);
        selectorRef.set(server, selector);

        server.stop();

        Assertions.assertTrue(selector.wokenUp);
    }

    @Test
    void testReadOpKeepsInterestOpsWhenReadReturnsNull() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel(new int[] {0}, new String[] {""});
        channel.configureBlocking(false);
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);

        Method readOp = SingleServer.class.getDeclaredMethod("readOp", SelectionKey.class);
        readOp.setAccessible(true);
        readOp.invoke(server, key);

        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
        Assertions.assertTrue(key.isValid());
    }

    @Test
    void testWriteOpWithNullAttachmentSwitchesBackToRead() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel(new int[] {0}, new String[] {""});
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_WRITE);
        key.attach(null);

        Method writeOp = SingleServer.class.getDeclaredMethod("writeOp", SelectionKey.class);
        writeOp.setAccessible(true);
        writeOp.invoke(server, key);

        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    @Test
    void testWriteOpWithEmptyAttachmentSwitchesBackToRead() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel(new int[] {0}, new String[] {""});
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_WRITE);
        key.attach("");

        Method writeOp = SingleServer.class.getDeclaredMethod("writeOp", SelectionKey.class);
        writeOp.setAccessible(true);
        writeOp.invoke(server, key);

        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    @Test
    void testCloseKeyHandlesChannelCloseIOException() throws Exception {
        SingleServer server = new SingleServer(0);
        ThrowOnCloseSocketChannel channel = new ThrowOnCloseSocketChannel();
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);

        Method closeKey = SingleServer.class.getDeclaredMethod("closeKey", SelectionKey.class);
        closeKey.setAccessible(true);
        closeKey.invoke(server, key);

        Assertions.assertFalse(key.isValid());
    }

    @Test
    void testCloseKeyCancelsNonSocketChannelKey() throws Exception {
        SingleServer server = new SingleServer(0);
        FakeServerSelectionKey key = new FakeServerSelectionKey(new NullAcceptServerSocketChannel(), SelectionKey.OP_ACCEPT);

        Method closeKey = SingleServer.class.getDeclaredMethod("closeKey", SelectionKey.class);
        closeKey.setAccessible(true);
        closeKey.invoke(server, key);

        Assertions.assertFalse(key.isValid());
    }

    @Test
    void testReadOpCancelsKeyOnEof() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel(new int[] {-1}, new String[] {""});
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);

        Method readOp = SingleServer.class.getDeclaredMethod("readOp", SelectionKey.class);
        readOp.setAccessible(true);
        readOp.invoke(server, key);

        Assertions.assertFalse(key.isValid());
    }

    @Test
    void testWriteOpByeClosesKeyAndDecrementsConnections() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel(new int[] {0}, new String[] {""});
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_WRITE);
        key.attach("bye");

        setConnectionsToOne(server);
        Method writeOp = SingleServer.class.getDeclaredMethod("writeOp", SelectionKey.class);
        writeOp.setAccessible(true);
        writeOp.invoke(server, key);

        Assertions.assertFalse(key.isValid());
        Assertions.assertEquals(0, getConnections(server));
    }

    @Test
    void testWriteOpClosesKeyOnWriteFailure() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = ScriptedSocketChannel.alwaysZeroWrites();
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_WRITE);
        key.attach("hello");

        Method writeOp = SingleServer.class.getDeclaredMethod("writeOp", SelectionKey.class);
        writeOp.setAccessible(true);
        writeOp.invoke(server, key);

        Assertions.assertFalse(key.isValid());
    }

    @Test
    void testWriteOpByeClosesKeyWhenGoodbyeWriteFails() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = ScriptedSocketChannel.alwaysZeroWrites();
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_WRITE);
        key.attach("bye");

        setConnectionsToOne(server);
        Method writeOp = SingleServer.class.getDeclaredMethod("writeOp", SelectionKey.class);
        writeOp.setAccessible(true);
        writeOp.invoke(server, key);

        Assertions.assertFalse(key.isValid());
        Assertions.assertEquals(0, getConnections(server));
    }

    @Test
    void testLoopHandlesSelectZeroAndStopsCleanly() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSelector selector = new ScriptedSelector(server, new int[] {0}, Collections.emptySet());
        NullAcceptServerSocketChannel serverChannel = new NullAcceptServerSocketChannel();

        Method loop = SingleServer.class.getDeclaredMethod("loop", Selector.class, ServerSocketChannel.class);
        loop.setAccessible(true);
        loop.invoke(server, selector, serverChannel);

        Assertions.assertTrue(selector.selectCalls >= 1);
    }

    @Test
    void testLoopProcessesReadableKey() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel(new int[] {6, 0}, new String[] {"ping\r\n", ""});
        channel.configureBlocking(false);
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_READ);
        Set<SelectionKey> selectedKeys = new java.util.LinkedHashSet<>();
        selectedKeys.add(key);
        ScriptedSelector selector = new ScriptedSelector(server, new int[] {1}, selectedKeys);

        Method loop = SingleServer.class.getDeclaredMethod("loop", Selector.class, ServerSocketChannel.class);
        loop.setAccessible(true);
        loop.invoke(server, selector, new NullAcceptServerSocketChannel());

        Assertions.assertEquals(SelectionKey.OP_WRITE, key.interestOps());
        Assertions.assertEquals("ping\r\n", key.attachment());
    }

    @Test
    void testLoopProcessesWritableKey() throws Exception {
        SingleServer server = new SingleServer(0);
        RecordingScriptedSocketChannel channel = new RecordingScriptedSocketChannel(new int[] {0}, new String[] {""});
        channel.configureBlocking(false);
        FakeSelectionKey key = new FakeSelectionKey(channel, SelectionKey.OP_WRITE);
        key.attach("stats\r\n");
        Set<SelectionKey> selectedKeys = new java.util.LinkedHashSet<>();
        selectedKeys.add(key);
        ScriptedSelector selector = new ScriptedSelector(server, new int[] {1}, selectedKeys);

        Method loop = SingleServer.class.getDeclaredMethod("loop", Selector.class, ServerSocketChannel.class);
        loop.setAccessible(true);
        loop.invoke(server, selector, new NullAcceptServerSocketChannel());

        Assertions.assertEquals("Simultaneously connected clients: 0\r\n", channel.writtenText());
        Assertions.assertEquals(SelectionKey.OP_READ, key.interestOps());
    }

    @Test
    void testLoopCatchesProcessingExceptionAndClosesKey() throws Exception {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel(new int[] {3, 0}, new String[] {"x\r\n", ""});
        channel.configureBlocking(false);
        ThrowingInterestOpsKey key = new ThrowingInterestOpsKey(channel, SelectionKey.OP_READ);
        Set<SelectionKey> selectedKeys = new java.util.LinkedHashSet<>();
        selectedKeys.add(key);
        ScriptedSelector selector = new ScriptedSelector(server, new int[] {1}, selectedKeys);

        Method loop = SingleServer.class.getDeclaredMethod("loop", Selector.class, ServerSocketChannel.class);
        loop.setAccessible(true);
        loop.invoke(server, selector, new NullAcceptServerSocketChannel());

        Assertions.assertFalse(key.isValid());
    }

    // ── run() error handling ─────────────────────────────────────────

    @Test
    void testConstructorFailsWhenPortAlreadyBound() {
        Assertions.assertThrows(java.io.UncheckedIOException.class, () -> new SingleServer(PORT),
                "Constructor should fail fast when the listening port is already in use");
    }

    // ── readOp cancel and connection counter ─────────────────────────

    @Test
    void testServerContinuesAfterClientAbruptClose() throws Exception {
        // Client connects and immediately closes → readOp gets EOF → key.cancel()
        Socket abrupt = new Socket("localhost", PORT);
        abrupt.setSoLinger(true, 0); // send RST on close
        abrupt.close();
        Thread.sleep(300);

        // Server should still be running
        try (Socket socket = new Socket("localhost", PORT)) {
            socket.setSoTimeout(2000);
            String response = sendAndReceive(socket, "still alive");
            Assertions.assertEquals("Did you say 'still alive'?", response);
        }
    }

    @Test
    void testConnectionCounterShowsActiveClients() throws IOException, InterruptedException {
        try (Socket s1 = new Socket("localhost", PORT);
             Socket s2 = new Socket("localhost", PORT)) {
            s1.setSoTimeout(2000);
            s2.setSoTimeout(2000);
            Thread.sleep(200);

            // With 2 clients connected, stats should show at least 2
            String response = sendAndReceive(s1, "stats");
            int count = Integer.parseInt(response.replace("Simultaneously connected clients: ", ""));
            Assertions.assertTrue(count >= 2,
                    "Expected at least 2 connections but got " + count);
        }
    }

    // ── read() edge cases ────────────────────────────────────────────

    @Test
    void testReadMultipleCallsReuseBuffer() {
        SingleServer server = new SingleServer(0);
        ScriptedSocketChannel channel = new ScriptedSocketChannel(
                new int[] {7, 8},
                new String[] {"first\r\n", "second\r\n"});

        String result1 = server.read(channel);
        Assertions.assertEquals("first\r\n", result1);

        String result2 = server.read(channel);
        Assertions.assertEquals("second\r\n", result2);
    }

    @Test
    void testReadHandlesUtf8Content() {
        SingleServer server = new SingleServer(0);
        String utf8Message = "Привет мир";
        String framedMessage = utf8Message + "\r\n";
        int byteCount = framedMessage.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        ScriptedSocketChannel channel = new ScriptedSocketChannel(
                new int[] {byteCount},
                new String[] {framedMessage});

        String result = server.read(channel);

        Assertions.assertEquals(framedMessage, result);
    }

    // ── Mockito-based tests for defensive catch blocks ────────────────

    @Test
    void testWriteReturnsEmptyOnGenericIOException() throws Exception {
        // Covers write() catch(IOException) L198-206 — generic IOException (not ClosedChannelException)
        SingleServer server = new SingleServer(0);
        SocketChannel mockChannel = mock(SocketChannel.class);
        when(mockChannel.write(any(ByteBuffer.class))).thenThrow(new IOException("broken pipe"));

        boolean result = server.write(mockChannel, "test");

        Assertions.assertFalse(result);
        verify(mockChannel).close();
    }

    @Test
    void testWriteHandlesCloseFailureAfterIOException() throws Exception {
        // Covers write() inner catch(IOException) on channel.close() L203-205
        SingleServer server = new SingleServer(0);
        SocketChannel mockChannel = mock(SocketChannel.class);
        when(mockChannel.write(any(ByteBuffer.class))).thenThrow(new IOException("broken pipe"));
        doThrow(new IOException("close failed")).when(mockChannel).close();

        boolean result = server.write(mockChannel, "test");

        Assertions.assertFalse(result);
    }

    @Test
    void testReadHandlesCloseFailureAfterIOException() throws Exception {
        // Covers read() inner catch(IOException) on channel.close() L141-143
        SingleServer server = new SingleServer(0);
        SocketChannel mockChannel = mock(SocketChannel.class);
        when(mockChannel.read(any(ByteBuffer.class))).thenThrow(new IOException("read failed"));
        doThrow(new IOException("close failed")).when(mockChannel).close();

        String result = server.read(mockChannel);

        Assertions.assertEquals("", result);
    }

    @Test
    void testWriteOpElseBranchClosesChannelAndCancelsKey() throws Exception {
        // Covers writeOp() else branch L181-184 — when write() returns false
        SingleServer server = new SingleServer(0);

        SocketChannel mockChannel = mock(SocketChannel.class);
        when(mockChannel.write(any(ByteBuffer.class))).thenThrow(new IOException("write failed"));

        SelectionKey mockKey = mock(SelectionKey.class);
        when(mockKey.attachment()).thenReturn("hello");
        when(mockKey.channel()).thenReturn(mockChannel);

        // Call private writeOp() via reflection
        Method writeOp = SingleServer.class.getDeclaredMethod("writeOp", SelectionKey.class);
        writeOp.setAccessible(true);
        writeOp.invoke(server, mockKey);

        // write() returned false → else branch executed: key.channel().close() + key.cancel()
        verify(mockKey).cancel();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testRunFinallyCatchesExceptionOnClose() throws Exception {
        // Covers run() finally catch(Exception) L62-64 — when selector.close() throws
        try (MockedStatic<Selector> mockedSel = Mockito.mockStatic(Selector.class);
             MockedStatic<ServerSocketChannel> mockedSSC = Mockito.mockStatic(ServerSocketChannel.class)) {

            Selector badSelector = mock(Selector.class);
            doThrow(new IOException("selector close failed")).when(badSelector).close();
            mockedSel.when(Selector::open).thenReturn(badSelector);

            ServerSocketChannel badSSC = mock(ServerSocketChannel.class);
            java.net.ServerSocket mockSocket = mock(java.net.ServerSocket.class);
            doThrow(new IOException("bind failed")).when(mockSocket).bind(any());
            when(badSSC.socket()).thenReturn(mockSocket);
            mockedSSC.when(ServerSocketChannel::open).thenReturn(badSSC);

            SingleServer server = new SingleServer(0);
            // run(): bind() throws → catch(Exception) → finally: selector.close() throws → catch(Exception) L62-64
            Assertions.assertDoesNotThrow(server::run);

            verify(badSelector).close();
        }
    }

    private static int getConnections(SingleServer server) throws Exception {
        var field = SingleServer.class.getDeclaredField("connections");
        field.setAccessible(true);
        return ((java.util.concurrent.atomic.AtomicInteger) field.get(server)).get();
    }

    private static void setConnectionsToOne(SingleServer server) throws Exception {
        var field = SingleServer.class.getDeclaredField("connections");
        field.setAccessible(true);
        ((java.util.concurrent.atomic.AtomicInteger) field.get(server)).set(1);
    }

    private static class FakeSelectionKey extends SelectionKey {
        private final SocketChannel channel;
        private int interestOps;
        private boolean valid = true;

        private FakeSelectionKey(SocketChannel channel, int interestOps) {
            this.channel = channel;
            this.interestOps = interestOps;
        }

        @Override
        public java.nio.channels.SelectableChannel channel() {
            return channel;
        }

        @Override
        public Selector selector() {
            return null;
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public void cancel() {
            valid = false;
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

    private static final class FakeServerSelectionKey extends SelectionKey {
        private final java.nio.channels.SelectableChannel channel;
        private int interestOps;
        private boolean valid = true;

        private FakeServerSelectionKey(java.nio.channels.SelectableChannel channel, int interestOps) {
            this.channel = channel;
            this.interestOps = interestOps;
        }

        @Override
        public java.nio.channels.SelectableChannel channel() {
            return channel;
        }

        @Override
        public Selector selector() {
            return null;
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public void cancel() {
            valid = false;
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

    private static final class ThrowingInterestOpsKey extends FakeSelectionKey {
        private ThrowingInterestOpsKey(SocketChannel channel, int interestOps) {
            super(channel, interestOps);
        }

        @Override
        public SelectionKey interestOps(int ops) {
            throw new RuntimeException("cannot update ops");
        }
    }

    private static final class NullAcceptServerSocketChannel extends ServerSocketChannel {
        private NullAcceptServerSocketChannel() {
            super(SelectorProvider.provider());
        }

        @Override
        public SocketChannel accept() {
            return null;
        }

        @Override
        public ServerSocket socket() {
            return null;
        }

        @Override
        public ServerSocketChannel bind(SocketAddress local, int backlog) {
            return this;
        }

        @Override
        public <T> ServerSocketChannel setOption(SocketOption<T> name, T value) {
            return this;
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
        public SocketAddress getLocalAddress() {
            return null;
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

    private static final class WakeupSelector extends Selector {
        private boolean wokenUp;

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public SelectorProvider provider() {
            return SelectorProvider.provider();
        }

        @Override
        public Set<SelectionKey> keys() {
            return Collections.emptySet();
        }

        @Override
        public Set<SelectionKey> selectedKeys() {
            return Collections.emptySet();
        }

        @Override
        public int selectNow() {
            return 0;
        }

        @Override
        public int select(long timeout) {
            return 0;
        }

        @Override
        public int select() {
            return 0;
        }

        @Override
        public Selector wakeup() {
            wokenUp = true;
            return this;
        }

        @Override
        public void close() {
            // no-op
        }
    }

    private static final class ScriptedSelector extends Selector {
        private final SingleServer server;
        private final int[] selectResults;
        private final Set<SelectionKey> selectedKeys;
        private int cursor;
        private int selectCalls;

        private ScriptedSelector(SingleServer server, int[] selectResults, Set<SelectionKey> selectedKeys) {
            this.server = server;
            this.selectResults = Arrays.copyOf(selectResults, selectResults.length);
            this.selectedKeys = selectedKeys;
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public SelectorProvider provider() {
            return SelectorProvider.provider();
        }

        @Override
        public Set<SelectionKey> keys() {
            return selectedKeys;
        }

        @Override
        public Set<SelectionKey> selectedKeys() {
            return selectedKeys;
        }

        @Override
        public int selectNow() {
            return 0;
        }

        @Override
        public int select(long timeout) {
            return select();
        }

        @Override
        public int select() {
            selectCalls++;
            if (cursor < selectResults.length) {
                int result = selectResults[cursor++];
                server.stop();
                return result;
            }
            return 0;
        }

        @Override
        public Selector wakeup() {
            return this;
        }

        @Override
        public void close() {
            // no-op
        }
    }

    private static class ScriptedSocketChannel extends SocketChannel {
        private static final int MAX_ZERO_WRITE_SPINS = 2048;

        private final int[] reads;
        private final byte[][] payloads;
        private int readIndex;
        private boolean alwaysZeroWrites;
        private boolean open = true;

        private ScriptedSocketChannel(int[] reads, String[] payloads) {
            super(SelectorProvider.provider());
            this.reads = Arrays.copyOf(reads, reads.length);
            this.payloads = new byte[payloads.length][];
            for (int i = 0; i < payloads.length; i++) {
                this.payloads[i] = payloads[i].getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        static ScriptedSocketChannel alwaysZeroWrites() {
            ScriptedSocketChannel channel = new ScriptedSocketChannel(new int[] {0}, new String[] {""});
            channel.alwaysZeroWrites = true;
            return channel;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            if (!open) {
                throw new ClosedChannelException();
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
            if (alwaysZeroWrites) {
                return 0;
            }
            int len = src.remaining();
            src.position(src.limit());
            return len;
        }

        @Override
        public long write(ByteBuffer[] srcs, int offset, int length) {
            long total = 0;
            int zeroSpins = 0;
            for (int i = offset; i < offset + length; i++) {
                while (srcs[i].hasRemaining()) {
                    int written = write(srcs[i]);
                    if (written == 0) {
                        zeroSpins++;
                        if (zeroSpins >= MAX_ZERO_WRITE_SPINS) {
                            return total;
                        }
                        continue;
                    }
                    total += written;
                }
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
        protected void implCloseSelectableChannel() throws IOException {
            open = false;
        }

        @Override
        protected void implConfigureBlocking(boolean block) {
            // no-op
        }
    }

    private static final class RecordingScriptedSocketChannel extends ScriptedSocketChannel {
        private final ByteBuffer writes = ByteBuffer.allocate(4096);

        private RecordingScriptedSocketChannel(int[] reads, String[] payloads) {
            super(reads, payloads);
        }

        String writtenText() {
            ByteBuffer copy = writes.duplicate();
            copy.flip();
            byte[] bytes = new byte[copy.remaining()];
            copy.get(bytes);
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        }

        @Override
        public int write(ByteBuffer src) {
            int len = src.remaining();
            writes.put(src);
            return len;
        }
    }

    private static final class ThrowOnCloseSocketChannel extends ScriptedSocketChannel {
        private ThrowOnCloseSocketChannel() {
            super(new int[] {0}, new String[] {""});
        }

        @Override
        protected void implCloseSelectableChannel() throws IOException {
            throw new IOException("close failed");
        }
    }

}
