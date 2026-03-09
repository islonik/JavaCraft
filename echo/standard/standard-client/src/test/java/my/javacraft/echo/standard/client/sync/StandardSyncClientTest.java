package my.javacraft.echo.standard.client.sync;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StandardSyncClientTest {

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

    // Waits until listener marks remote EOF, so the assertion checks the steady state after server close.
    private static void awaitServerCloseObserved(StandardSyncClient client) {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            while (!client.isClosedByServer()) {
                Thread.onSpinWait();
            }
        });
    }
}
