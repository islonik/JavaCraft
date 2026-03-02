package my.javacraft.echo.single.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
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

        out.write(message.getBytes());
        out.flush();
        Thread.sleep(200);

        byte[] buf = new byte[1024];
        int len = in.read(buf);
        return new String(buf, 0, len).trim();
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
            Assertions.assertTrue(response.endsWith("simultaneously connected clients."));
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
    void testReadReturnsContent() throws Exception {
        SingleServer server = new SingleServer(0);
        try (ServerSocket ss = new ServerSocket(0)) {
            SocketChannel client = SocketChannel.open(
                    new InetSocketAddress("localhost", ss.getLocalPort()));
            Socket serverSide = ss.accept();

            serverSide.getOutputStream().write("hello".getBytes());
            serverSide.getOutputStream().flush();
            Thread.sleep(50);

            String result = server.read(client);
            Assertions.assertEquals("hello", result);

            client.close();
            serverSide.close();
        }
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

    // ── run() error handling ─────────────────────────────────────────

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testRunHandlesBindException() throws InterruptedException {
        // PORT is already in use by our @BeforeAll server
        SingleServer duplicate = new SingleServer(PORT);
        Thread t = new Thread(duplicate::run);
        t.start();
        t.join(2000);
        // run() should have caught BindException and returned
        Assertions.assertFalse(t.isAlive(),
                "run() should complete after BindException");
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
            int count = Integer.parseInt(response.replace(" simultaneously connected clients.", ""));
            Assertions.assertTrue(count >= 2,
                    "Expected at least 2 connections but got " + count);
        }
    }

    // ── read() edge cases ────────────────────────────────────────────

    @Test
    void testReadMultipleCallsReuseBuffer() throws Exception {
        SingleServer server = new SingleServer(0);
        try (ServerSocket ss = new ServerSocket(0)) {
            SocketChannel client = SocketChannel.open(
                    new InetSocketAddress("localhost", ss.getLocalPort()));
            Socket serverSide = ss.accept();

            // First read
            serverSide.getOutputStream().write("first".getBytes());
            serverSide.getOutputStream().flush();
            Thread.sleep(50);
            String result1 = server.read(client);
            Assertions.assertEquals("first", result1);

            // Second read — verify buffer.clear() properly resets
            serverSide.getOutputStream().write("second".getBytes());
            serverSide.getOutputStream().flush();
            Thread.sleep(50);
            String result2 = server.read(client);
            Assertions.assertEquals("second", result2);

            client.close();
            serverSide.close();
        }
    }

    @Test
    void testReadHandlesUtf8Content() throws Exception {
        SingleServer server = new SingleServer(0);
        try (ServerSocket ss = new ServerSocket(0)) {
            SocketChannel client = SocketChannel.open(
                    new InetSocketAddress("localhost", ss.getLocalPort()));
            Socket serverSide = ss.accept();

            String utf8Message = "Привет мир";
            serverSide.getOutputStream().write(utf8Message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            serverSide.getOutputStream().flush();
            Thread.sleep(50);

            String result = server.read(client);
            Assertions.assertEquals(utf8Message, result);

            client.close();
            serverSide.close();
        }
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

}
