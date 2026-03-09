package my.javacraft.echo.standard.client.sync;

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
}
