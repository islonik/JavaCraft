package my.javacraft.echo.standard.client.sync;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
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
}
