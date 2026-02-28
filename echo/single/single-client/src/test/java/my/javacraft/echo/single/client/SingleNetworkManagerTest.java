package my.javacraft.echo.single.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SingleNetworkManagerTest {

    private SingleNetworkManager manager;

    @BeforeEach
    void setUp() {
        manager = new SingleNetworkManager();
    }

    // ── Message queue tests ──────────────────────────────────────────

    @Test
    void testAddAndGetMessage() {
        manager.addMessage("hello");
        String msg = manager.getMessage();
        Assertions.assertEquals("hello", msg);
    }

    @Test
    void testGetMessageReturnsNullWhenEmpty() {
        String msg = manager.getMessage();
        Assertions.assertNull(msg);
    }

    @Test
    void testMessagesReturnedInFifoOrder() {
        manager.addMessage("first");
        manager.addMessage("second");
        manager.addMessage("third");

        Assertions.assertEquals("first", manager.getMessage());
        Assertions.assertEquals("second", manager.getMessage());
        Assertions.assertEquals("third", manager.getMessage());
        Assertions.assertNull(manager.getMessage());
    }

    @Test
    void testAddMessageEvictsOldestOnOverflow() {
        // Queue capacity is 10
        for (int i = 0; i < 10; i++) {
            manager.addMessage("msg-" + i);
        }
        // Adding 11th should evict msg-0
        manager.addMessage("msg-10");

        String first = manager.getMessage();
        Assertions.assertEquals("msg-1", first);
    }

    // ── Socket open / close tests ────────────────────────────────────

    @Test
    void testOpenAndCloseSocket() throws IOException {
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            acceptAsync(server);

            manager.openSocket("localhost", port);

            SocketChannel channel = manager.getSocketChannel();
            Assertions.assertNotNull(channel);
            Assertions.assertTrue(channel.isOpen());

            Selector selector = manager.getSelector();
            Assertions.assertNotNull(selector);
            Assertions.assertTrue(selector.isOpen());

            manager.closeSocket();
        }
    }

    @Test
    void testOpenSocketIsIdempotent() throws IOException {
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            acceptAsync(server);

            manager.openSocket("localhost", port);
            SocketChannel first = manager.getSocketChannel();

            // Second call should be no-op (client already set)
            manager.openSocket("localhost", port);
            SocketChannel second = manager.getSocketChannel();

            Assertions.assertSame(first, second);
            manager.closeSocket();
        }
    }

    @Test
    void testCloseSocketWhenNotOpened() {
        // Should not throw when nothing was opened
        Assertions.assertDoesNotThrow(() -> manager.closeSocket());
    }

    // ── Blocking wait/notify tests ───────────────────────────────────

    @Test
    void testGetSelectorBlocksUntilSocketOpened() throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            acceptAsync(server);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                Future<Selector> future = executor.submit(() -> manager.getSelector());

                // Give the thread time to enter wait()
                Thread.sleep(100);
                Assertions.assertFalse(future.isDone(), "getSelector() should block until openSocket() is called");

                // Open socket — this calls notifyAll() and should unblock getSelector()
                manager.openSocket("localhost", port);

                Selector sel = future.get(2, TimeUnit.SECONDS);
                Assertions.assertNotNull(sel);
                Assertions.assertTrue(sel.isOpen());
            } finally {
                manager.closeSocket();
                executor.shutdownNow();
            }
        }
    }

    @Test
    void testGetSocketChannelBlocksUntilSocketOpened() throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            acceptAsync(server);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                Future<SocketChannel> future = executor.submit(() -> manager.getSocketChannel());

                // Give the thread time to enter wait()
                Thread.sleep(100);
                Assertions.assertFalse(future.isDone(), "getSocketChannel() should block until openSocket() is called");

                // Open socket — this calls notifyAll() and should unblock getSocketChannel()
                manager.openSocket("localhost", port);

                SocketChannel ch = future.get(2, TimeUnit.SECONDS);
                Assertions.assertNotNull(ch);
                Assertions.assertTrue(ch.isOpen());
            } finally {
                manager.closeSocket();
                executor.shutdownNow();
            }
        }
    }

    // ── Setter / getter test ─────────────────────────────────────────

    @Test
    void testSetAndGetSingleMessageSender() {
        Assertions.assertNull(manager.getSingleMessageSender());

        SingleMessageSender sender = new SingleMessageSender();
        manager.setSingleMessageSender(sender);

        Assertions.assertSame(sender, manager.getSingleMessageSender());
    }

    // ── Helper ───────────────────────────────────────────────────────

    private static void acceptAsync(ServerSocket server) {
        Thread thread = new Thread(() -> {
            try {
                server.accept();
            } catch (IOException ignored) {
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
