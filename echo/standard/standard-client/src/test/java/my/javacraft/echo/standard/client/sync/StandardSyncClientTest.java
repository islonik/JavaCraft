package my.javacraft.echo.standard.client.sync;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StandardSyncClientTest {

    @Test
    void testSendMessageShouldWriteToServer() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            AtomicReference<String> receivedMessage = new AtomicReference<>();
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();

            Thread acceptThread = Thread.ofVirtual().start(() -> {
                try (Socket accepted = serverSocket.accept();
                     BufferedReader serverReader = new BufferedReader(new InputStreamReader(accepted.getInputStream()))) {
                    receivedMessage.set(serverReader.readLine());
                } catch (Exception e) {
                    acceptThreadFailure.set(e);
                }
            });

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {
                client.sendMessage("hello");
            }

            acceptThread.join(Duration.ofSeconds(2));
            Assertions.assertFalse(acceptThread.isAlive());
            Assertions.assertEquals("hello", receivedMessage.get());
            Assertions.assertNull(acceptThreadFailure.get());
        }
    }

    @Test
    void testReadMessageShouldReturnServerResponse() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();
            Thread acceptThread = Thread.ofVirtual().start(() -> {
                try (Socket accepted = serverSocket.accept();
                     PrintWriter serverWriter = new PrintWriter(accepted.getOutputStream(), true)) {
                    serverWriter.println("pong");
                } catch (Exception e) {
                    acceptThreadFailure.set(e);
                }
            });

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {
                Assertions.assertEquals("pong", client.readMessage());
            }

            acceptThread.join(Duration.ofSeconds(2));
            Assertions.assertFalse(acceptThread.isAlive());
            Assertions.assertNull(acceptThreadFailure.get());
        }
    }

    @Test
    void testReadMessageShouldReturnNullAndKeepInterruptStatus() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            CountDownLatch releaseServerSocket = new CountDownLatch(1);
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();

            Thread acceptThread = Thread.ofVirtual().start(() -> {
                try (Socket ignored = serverSocket.accept()) {
                    Assertions.assertTrue(releaseServerSocket.await(2, TimeUnit.SECONDS));
                } catch (Exception e) {
                    acceptThreadFailure.set(e);
                }
            });

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {
                Thread.currentThread().interrupt();
                Assertions.assertNull(client.readMessage());
                Assertions.assertTrue(Thread.currentThread().isInterrupted());
                Assertions.assertTrue(Thread.interrupted());
            } finally {
                releaseServerSocket.countDown();
            }

            acceptThread.join(Duration.ofSeconds(2));
            Assertions.assertFalse(acceptThread.isAlive());
            Assertions.assertNull(acceptThreadFailure.get());
        }
    }

    @Test
    void testConstructorShouldThrowWhenConnectionCannotBeEstablished() {

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> {
                    try (StandardSyncClient ignored = new StandardSyncClient(
                            "sync-client-", "127.0.0.1", 1)) {
                        Assertions.fail("Constructor should fail before entering try block");
                    }
                }
        );

        Assertions.assertTrue(exception.getMessage().contains("127.0.0.1:1"));
        Assertions.assertNotNull(exception.getCause());
    }

    @Test
    void testRunShouldHandleIllegalStateExceptionFromSendMessage() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0);
             StandardSyncClient client = new StandardSyncClient(
                     "sync-client-",
                     "127.0.0.1",
                     serverSocket.getLocalPort())) {
            client.close();
            InputStream originalIn = System.in;
            try {
                System.setIn(new ByteArrayInputStream("hello\n".getBytes(StandardCharsets.UTF_8)));
                Assertions.assertDoesNotThrow(client::run);
            } finally {
                System.setIn(originalIn);
            }
        }
    }

    @Test
    void testRunShouldProcessByeCommandAndCloseConnection() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            AtomicReference<String> receivedMessage = new AtomicReference<>();
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();
            Thread acceptThread = Thread.ofVirtual().start(() -> {
                try (Socket accepted = serverSocket.accept();
                     BufferedReader serverReader = new BufferedReader(new InputStreamReader(accepted.getInputStream()));
                     PrintWriter serverWriter = new PrintWriter(accepted.getOutputStream(), true)) {
                    String message = serverReader.readLine();
                    receivedMessage.set(message);
                    serverWriter.println("echo:" + message);
                } catch (Exception e) {
                    acceptThreadFailure.set(e);
                }
            });

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {
                InputStream originalIn = System.in;
                try {
                    System.setIn(new ByteArrayInputStream("bye\n".getBytes(StandardCharsets.UTF_8)));
                    Assertions.assertDoesNotThrow(client::run);
                } finally {
                    System.setIn(originalIn);
                }
                Assertions.assertFalse(client.isConnected());
            }

            acceptThread.join(Duration.ofSeconds(2));
            Assertions.assertFalse(acceptThread.isAlive());
            Assertions.assertEquals("bye", receivedMessage.get());
            Assertions.assertNull(acceptThreadFailure.get());
        }
    }

    @Test
    void testRunShouldHandleEofWithoutSendingMessage() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            AtomicReference<String> receivedMessage = new AtomicReference<>();
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();
            Thread acceptThread = Thread.ofVirtual().start(() -> {
                try (Socket accepted = serverSocket.accept();
                     BufferedReader serverReader = new BufferedReader(new InputStreamReader(accepted.getInputStream()))) {
                    receivedMessage.set(serverReader.readLine());
                } catch (Exception e) {
                    acceptThreadFailure.set(e);
                }
            });

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {
                InputStream originalIn = System.in;
                try {
                    System.setIn(new ByteArrayInputStream(new byte[0]));
                    Assertions.assertDoesNotThrow(client::run);
                } finally {
                    System.setIn(originalIn);
                }
            }

            acceptThread.join(Duration.ofSeconds(2));
            Assertions.assertFalse(acceptThread.isAlive());
            Assertions.assertNull(receivedMessage.get());
            Assertions.assertNull(acceptThreadFailure.get());
        }
    }

    @Test
    void testRunShouldHandleIOExceptionFromConsoleInput() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            CountDownLatch releaseServerSocket = new CountDownLatch(1);
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();
            Thread acceptThread = Thread.ofVirtual().start(() -> {
                try (Socket ignored = serverSocket.accept()) {
                    Assertions.assertTrue(releaseServerSocket.await(2, TimeUnit.SECONDS));
                } catch (Exception e) {
                    acceptThreadFailure.set(e);
                }
            });

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {
                InputStream originalIn = System.in;
                try {
                    System.setIn(new InputStream() {
                        @Override
                        public int read() throws IOException {
                            throw new IOException("forced read failure");
                        }
                    });
                    Assertions.assertDoesNotThrow(client::run);
                } finally {
                    System.setIn(originalIn);
                }
            } finally {
                releaseServerSocket.countDown();
            }

            acceptThread.join(Duration.ofSeconds(2));
            Assertions.assertFalse(acceptThread.isAlive());
            Assertions.assertNull(acceptThreadFailure.get());
        }
    }

    @Test
    void testIsConnectedShouldBeFalseAfterRemoteClose() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();
            Thread acceptThread = Thread.ofVirtual().start(() -> {
                try (Socket accepted = serverSocket.accept()) {
                    SocketAddress remoteAddress = accepted.getRemoteSocketAddress();
                    if (remoteAddress == null) {
                        throw new IllegalStateException("Accepted socket must have remote address");
                    }
                } catch (Exception e) {
                    acceptThreadFailure.set(e);
                }
            });

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {

                Assertions.assertTrue(client.isConnected());
                awaitServerCloseObserved(client);
                Assertions.assertFalse(client.isConnected());
            }

            acceptThread.join(Duration.ofSeconds(2));
            Assertions.assertFalse(acceptThread.isAlive());
            Assertions.assertNull(acceptThreadFailure.get());
        }
    }

    @Test
    void testSendMessageShouldThrowWhenWriterSignalsError() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            CountDownLatch releaseServerSocket = new CountDownLatch(1);
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();

            Thread acceptThread = Thread.ofVirtual().start(() -> {
                try (Socket ignored = serverSocket.accept()) {
                    Assertions.assertTrue(releaseServerSocket.await(2, TimeUnit.SECONDS));
                } catch (Exception e) {
                    acceptThreadFailure.set(e);
                }
            });

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {

                replaceWriterWithFailingWriter(client);

                IllegalStateException exception = Assertions.assertThrows(
                        IllegalStateException.class,
                        () -> client.sendMessage("hello")
                );
                Assertions.assertTrue(exception.getMessage().contains("Failed to send message"));
            } finally {
                releaseServerSocket.countDown();
            }

            acceptThread.join(Duration.ofSeconds(2));
            Assertions.assertFalse(acceptThread.isAlive());
            Assertions.assertNull(acceptThreadFailure.get());
        }
    }

    @Test
    void testCloseShouldNotMarkClosedByServerWhenClientInitiatesShutdown() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            CountDownLatch releaseServerSocket = new CountDownLatch(1);
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();

            Thread acceptThread = Thread.ofVirtual().start(() -> {
                try (Socket ignored = serverSocket.accept()) {
                    Assertions.assertTrue(releaseServerSocket.await(2, TimeUnit.SECONDS));
                } catch (Exception e) {
                    acceptThreadFailure.set(e);
                }
            });

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {
                Assertions.assertFalse(client.isClosedByServer());
                client.close();
                Assertions.assertFalse(client.isClosedByServer());
            } finally {
                releaseServerSocket.countDown();
            }

            acceptThread.join(Duration.ofSeconds(2));
            Assertions.assertFalse(acceptThread.isAlive());
            Assertions.assertNull(acceptThreadFailure.get());
        }
    }

    @Test
    void testResponseQueueShouldBeBounded() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            CountDownLatch releaseServerSocket = new CountDownLatch(1);
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();

            Thread acceptThread = Thread.ofVirtual().start(() -> {
                try (Socket ignored = serverSocket.accept()) {
                    Assertions.assertTrue(releaseServerSocket.await(2, TimeUnit.SECONDS));
                } catch (Exception e) {
                    acceptThreadFailure.set(e);
                }
            });

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {
                BlockingQueue<?> responseQueue = getResponseQueue(client);
                Assertions.assertNotEquals(Integer.MAX_VALUE, responseQueue.remainingCapacity());
            } finally {
                releaseServerSocket.countDown();
            }

            acceptThread.join(Duration.ofSeconds(2));
            Assertions.assertFalse(acceptThread.isAlive());
            Assertions.assertNull(acceptThreadFailure.get());
        }
    }

    @Test
    void testResponseQueueOverflowShouldCloseClient() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {

                int queueLimit = getResponseQueue(client).remainingCapacity();
                Thread acceptThread = Thread.ofVirtual().start(() -> {
                    try (Socket accepted = serverSocket.accept();
                         PrintWriter serverWriter = new PrintWriter(accepted.getOutputStream(), true)) {
                        for (int i = 0; i <= queueLimit; i++) {
                            serverWriter.println("response-" + i);
                        }
                    } catch (Exception e) {
                        acceptThreadFailure.set(e);
                    }
                });

                awaitCondition(Duration.ofSeconds(2), () -> !client.isConnected());
                Assertions.assertFalse(client.isConnected());

                acceptThread.join(Duration.ofSeconds(2));
                Assertions.assertFalse(acceptThread.isAlive());
                Assertions.assertNull(acceptThreadFailure.get());
            }
        }
    }

    @Test
    void testCloseShouldBeIdempotent() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            CountDownLatch releaseServerSocket = new CountDownLatch(1);
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();

            Thread acceptThread = Thread.ofVirtual().start(() -> {
                try (Socket ignored = serverSocket.accept()) {
                    Assertions.assertTrue(releaseServerSocket.await(2, TimeUnit.SECONDS));
                } catch (Exception e) {
                    acceptThreadFailure.set(e);
                }
            });

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {
                client.close();
                Assertions.assertDoesNotThrow(client::close);
            } finally {
                releaseServerSocket.countDown();
            }

            acceptThread.join(Duration.ofSeconds(2));
            Assertions.assertFalse(acceptThread.isAlive());
            Assertions.assertNull(acceptThreadFailure.get());
        }
    }

    @Test
    void testCloseShouldHandleWriterAndSocketCloseFailures() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            CountDownLatch releaseServerSocket = new CountDownLatch(1);
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();
            Thread acceptThread = Thread.ofVirtual().start(() -> {
                try (Socket ignored = serverSocket.accept()) {
                    Assertions.assertTrue(releaseServerSocket.await(2, TimeUnit.SECONDS));
                } catch (Exception e) {
                    acceptThreadFailure.set(e);
                }
            });

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {
                closeLiveSocketSilently(getSocket(client));
                setSocket(client, new ThrowingCloseSocket());
                setWriter(client, new ThrowingClosePrintWriter());

                Assertions.assertDoesNotThrow(client::close);
                Assertions.assertNull(getSocket(client));
                Assertions.assertNull(getWriter(client));
            } finally {
                releaseServerSocket.countDown();
            }

            acceptThread.join(Duration.ofSeconds(2));
            Assertions.assertFalse(acceptThread.isAlive());
            Assertions.assertNull(acceptThreadFailure.get());
        }
    }

    @Test
    void testListenerShouldMarkClosedByServerOnReadIOException() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            CountDownLatch releaseServerSocket = new CountDownLatch(1);
            AtomicReference<Throwable> acceptThreadFailure = new AtomicReference<>();
            Thread acceptThread = Thread.ofVirtual().start(() -> {
                try (Socket ignored = serverSocket.accept()) {
                    Assertions.assertTrue(releaseServerSocket.await(2, TimeUnit.SECONDS));
                } catch (Exception e) {
                    acceptThreadFailure.set(e);
                }
            });

            try (StandardSyncClient client = new StandardSyncClient(
                    "sync-client-",
                    "127.0.0.1",
                    serverSocket.getLocalPort())) {
                setSocket(client, new IOExceptionOnReadSocket());

                invokeAwaitResponseFromServer(client);

                awaitCondition(Duration.ofSeconds(2), client::isClosedByServer);
                Assertions.assertTrue(client.isClosedByServer());
            } finally {
                releaseServerSocket.countDown();
            }

            acceptThread.join(Duration.ofSeconds(2));
            Assertions.assertFalse(acceptThread.isAlive());
            Assertions.assertNull(acceptThreadFailure.get());
        }
    }

    // Waits until listener marks remote EOF, so the assertion checks the steady state after server close.
    private static void awaitServerCloseObserved(StandardSyncClient client) {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            while (!client.isClosedByServer()) {
                Thread.onSpinWait();
            }
        });
    }

    // Reflectively reads internal queue to assert capacity and overflow behavior without exposing test-only API.
    private static BlockingQueue<?> getResponseQueue(StandardSyncClient client) {
        try {
            Field queueField = StandardSyncClient.class.getDeclaredField("responseQueue");
            queueField.setAccessible(true);
            return (BlockingQueue<?>) queueField.get(client);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to inspect response queue", e);
        }
    }

    private static Socket getSocket(StandardSyncClient client) {
        try {
            Field socketField = StandardSyncClient.class.getDeclaredField("socket");
            socketField.setAccessible(true);
            return (Socket) socketField.get(client);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to inspect socket", e);
        }
    }

    private static void setSocket(StandardSyncClient client, Socket socket) {
        try {
            Field socketField = StandardSyncClient.class.getDeclaredField("socket");
            socketField.setAccessible(true);
            socketField.set(client, socket);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to replace socket", e);
        }
    }

    private static PrintWriter getWriter(StandardSyncClient client) {
        try {
            Field writerField = StandardSyncClient.class.getDeclaredField("clientWritingStreamToServerSocket");
            writerField.setAccessible(true);
            return (PrintWriter) writerField.get(client);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to inspect writer", e);
        }
    }

    private static void setWriter(StandardSyncClient client, PrintWriter writer) {
        try {
            Field writerField = StandardSyncClient.class.getDeclaredField("clientWritingStreamToServerSocket");
            writerField.setAccessible(true);
            writerField.set(client, writer);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to replace writer", e);
        }
    }

    private static void invokeAwaitResponseFromServer(StandardSyncClient client) {
        try {
            Method method = StandardSyncClient.class.getDeclaredMethod("awaitResponseFromServer", String.class);
            method.setAccessible(true);
            method.invoke(client, "sync-client-io-failure-");
        } catch (InvocationTargetException e) {
            throw new AssertionError("Listener startup failed", e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to invoke listener startup", e);
        }
    }

    // Polls until condition becomes true, so socket-thread transitions can settle without Thread.sleep().
    private static void awaitCondition(Duration timeout, BooleanSupplier condition) {
        Assertions.assertTimeoutPreemptively(timeout, () -> {
            while (!condition.getAsBoolean()) {
                Thread.onSpinWait();
            }
        });
    }

    // Injects a writer that always fails so the test can assert sendMessage handles PrintWriter error state.
    private static void replaceWriterWithFailingWriter(StandardSyncClient client) {
        PrintWriter alwaysFailingWriter = new PrintWriter(new Writer() {
            @Override
            public void write(char @NonNull [] cbuf, int off, int len) throws IOException {
                throw new IOException("forced write failure");
            }

            @Override
            public void flush() throws IOException {
                throw new IOException("forced flush failure");
            }

            @Override
            public void close() {
                // no-op for test stub
            }
        }, true);

        try {
            Field writerField = StandardSyncClient.class.getDeclaredField("clientWritingStreamToServerSocket");
            writerField.setAccessible(true);
            writerField.set(client, alwaysFailingWriter);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to inject test writer", e);
        }
    }

    private static void closeLiveSocketSilently(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ignored) {
            // no-op in test helper
        }
    }

    private static final class ThrowingCloseSocket extends Socket {
        @Override
        public synchronized void close() throws IOException {
            throw new IOException("forced close failure");
        }
    }

    private static final class ThrowingClosePrintWriter extends PrintWriter {
        private ThrowingClosePrintWriter() {
            super(Writer.nullWriter());
        }

        @Override
        public void close() {
            throw new RuntimeException("forced writer close failure");
        }
    }

    private static final class IOExceptionOnReadSocket extends Socket {
        @Override
        public InputStream getInputStream() {
            return new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException("forced read failure");
                }
            };
        }
    }
}
