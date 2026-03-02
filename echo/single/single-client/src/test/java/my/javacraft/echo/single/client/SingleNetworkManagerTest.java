package my.javacraft.echo.single.client;

import java.io.IOException;
import java.lang.reflect.Field;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

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

    @Test
    void testAddMessageMultipleOverflows() {
        // Add 15 messages to a capacity-10 queue → first 5 should be evicted
        for (int i = 0; i < 15; i++) {
            manager.addMessage("msg-" + i);
        }

        // Queue should contain msg-5 through msg-14
        for (int i = 5; i < 15; i++) {
            Assertions.assertEquals("msg-" + i, manager.getMessage());
        }
        Assertions.assertNull(manager.getMessage());
    }

    @Test
    void testGetMessagePollTimeout() {
        long start = System.currentTimeMillis();
        String result = manager.getMessage();
        long elapsed = System.currentTimeMillis() - start;

        Assertions.assertNull(result);
        // POLL_TIMEOUT_MS is 100 — getMessage should wait ~100ms before returning null
        Assertions.assertTrue(elapsed >= 80, "Expected poll to wait ~100ms but took " + elapsed + "ms");
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

    @Test
    void testGetSelectorFastPathWhenAlreadyOpen() throws IOException {
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            acceptAsync(server);

            manager.openSocket("localhost", port);

            // First call — selector is set, enters fast path (no wait)
            Selector sel1 = manager.getSelector();
            // Second call — still fast path (if selector != null check in getSelector())
            Selector sel2 = manager.getSelector();

            Assertions.assertSame(sel1, sel2);
            manager.closeSocket();
        }
    }

    @Test
    void testGetSocketChannelFastPathWhenAlreadyOpen() throws IOException {
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            acceptAsync(server);

            manager.openSocket("localhost", port);

            // First call — client is set, enters fast path (no wait)
            SocketChannel ch1 = manager.getSocketChannel();
            // Second call — still fast path
            SocketChannel ch2 = manager.getSocketChannel();

            Assertions.assertSame(ch1, ch2);
            manager.closeSocket();
        }
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

    // ── Interrupt handling tests ─────────────────────────────────────

    @Test
    void testGetSelectorReturnsNullOnInterrupt() {
        Thread.currentThread().interrupt();

        Assertions.assertNull(manager.getSelector());

        // Clear interrupt flag (getSelector re-sets it before throwing)
        Thread.interrupted();
    }

    @Test
    void testGetSocketChannelThrowsRuntimeExceptionOnInterrupt() {
        Thread.currentThread().interrupt();

        Assertions.assertThrows(RuntimeException.class, () -> manager.getSocketChannel());

        // Clear interrupt flag
        Thread.interrupted();
    }

    @Test
    void testGetMessageReturnsNullOnInterrupt() {
        Thread.currentThread().interrupt();

        String result = manager.getMessage();
        Assertions.assertNull(result);

        // Clear interrupt flag (getMessage re-sets it)
        Assertions.assertTrue(Thread.interrupted());
    }

    @Test
    void testCloseSocketTwiceIsNoOp() throws IOException {
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            acceptAsync(server);

            manager.openSocket("localhost", port);

            // First close
            Assertions.assertDoesNotThrow(() -> manager.closeSocket());
            // Second close — should be no-op (client is already null)
            Assertions.assertDoesNotThrow(() -> manager.closeSocket());
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

    // ── Mockito-based test for closeSocket() catch(IOException) ─────

    @Test
    void testCloseSocketCatchesIOExceptionFromSelectorClose() throws Exception {
        // Covers closeSocket() catch(IOException) L112-113
        Selector mockSelector = mock(Selector.class);
        SocketChannel mockClient = mock(SocketChannel.class);
        doThrow(new IOException("selector close failed")).when(mockSelector).close();

        // Use reflection to inject mock objects into private volatile fields
        Field clientField = SingleNetworkManager.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(manager, mockClient);

        Field selectorField = SingleNetworkManager.class.getDeclaredField("selector");
        selectorField.setAccessible(true);
        selectorField.set(manager, mockSelector);

        // closeSocket() should catch IOException and not propagate it
        Assertions.assertDoesNotThrow(() -> manager.closeSocket());
    }

    @Test
    void testCloseSocketCatchesIOExceptionFromClientClose() throws Exception {
        // Covers closeSocket() catch(IOException) when client.close() throws
        Selector mockSelector = mock(Selector.class);
        SocketChannel mockClient = mock(SocketChannel.class);
        doThrow(new IOException("client close failed")).when(mockClient).close();

        Field clientField = SingleNetworkManager.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(manager, mockClient);

        Field selectorField = SingleNetworkManager.class.getDeclaredField("selector");
        selectorField.setAccessible(true);
        selectorField.set(manager, mockSelector);

        Assertions.assertDoesNotThrow(() -> manager.closeSocket());
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
