package my.javacraft.echo.single.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        server.getOutputStream().write("hello world".getBytes());
        server.getOutputStream().flush();
        Thread.sleep(50);

        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());
        String result = listener.newResponse(client);

        Assertions.assertEquals("hello world", result);

        client.close();
        server.close();
    }

    @Test
    void testNewResponseTrimsWhitespace() throws Exception {
        SocketChannel client = SocketChannel.open(
                new InetSocketAddress("localhost", serverSocket.getLocalPort()));
        Socket server = serverSocket.accept();

        server.getOutputStream().write("  padded message  \n".getBytes());
        server.getOutputStream().flush();
        Thread.sleep(50);

        SingleMessageListener listener = new SingleMessageListener(new SingleNetworkManager());
        String result = listener.newResponse(client);

        Assertions.assertEquals("padded message", result);

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

            mgr.openSocket("localhost", String.valueOf(serverSocket.getLocalPort()));
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
    void testRunQueuesReceivedMessages() throws Exception {
        SingleNetworkManager mgr = new SingleNetworkManager();
        SingleMessageSender sender = new SingleMessageSender();
        mgr.setSingleMessageSender(sender);

        SingleMessageListener listener = new SingleMessageListener(mgr);
        ExecutorService listenerExec = Executors.newSingleThreadExecutor();
        try {
            listenerExec.execute(listener);

            mgr.openSocket("localhost", String.valueOf(serverSocket.getLocalPort()));
            Socket serverSide = serverSocket.accept();

            // Wait for OP_CONNECT
            Thread.sleep(300);

            // Client sends message to server
            sender.send("hello");
            Thread.sleep(100);

            // Server reads and verifies
            byte[] buf = new byte[256];
            int len = serverSide.getInputStream().read(buf);
            Assertions.assertEquals("hello", new String(buf, 0, len));

            // Server sends response back
            serverSide.getOutputStream().write("echo: hello".getBytes());
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
    void testRunQueuesMultipleMessages() throws Exception {
        SingleNetworkManager mgr = new SingleNetworkManager();
        SingleMessageSender sender = new SingleMessageSender();
        mgr.setSingleMessageSender(sender);

        SingleMessageListener listener = new SingleMessageListener(mgr);
        ExecutorService listenerExec = Executors.newSingleThreadExecutor();
        try {
            listenerExec.execute(listener);

            mgr.openSocket("localhost", String.valueOf(serverSocket.getLocalPort()));
            Socket serverSide = serverSocket.accept();

            // Wait for OP_CONNECT
            Thread.sleep(300);

            // First round-trip
            sender.send("msg1");
            Thread.sleep(100);
            byte[] buf = new byte[256];
            int len = serverSide.getInputStream().read(buf);
            Assertions.assertEquals("msg1", new String(buf, 0, len));

            serverSide.getOutputStream().write("reply1".getBytes());
            serverSide.getOutputStream().flush();
            Thread.sleep(300);

            Assertions.assertEquals("reply1", mgr.getMessage());

            // Second round-trip
            sender.send("msg2");
            Thread.sleep(100);
            len = serverSide.getInputStream().read(buf);
            Assertions.assertEquals("msg2", new String(buf, 0, len));

            serverSide.getOutputStream().write("reply2".getBytes());
            serverSide.getOutputStream().flush();
            Thread.sleep(300);

            Assertions.assertEquals("reply2", mgr.getMessage());

            serverSide.close();
        } finally {
            mgr.closeSocket();
            listenerExec.shutdownNow();
        }
    }
}
