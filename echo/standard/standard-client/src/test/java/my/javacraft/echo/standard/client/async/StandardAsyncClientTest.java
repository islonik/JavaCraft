package my.javacraft.echo.standard.client.async;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class StandardAsyncClientTest {

    @Test
    void testRunShouldHandleEofWithoutSendingMessage() {
        try (MockedConstruction<AsyncClientConnection> ignored = Mockito.mockConstruction(AsyncClientConnection.class)) {
            StandardAsyncClient client = new StandardAsyncClient("async-client", "127.0.0.1", 8075);
            AsyncClientConnection mockedConnection = ignored.constructed().getFirst();

            InputStream originalIn = System.in;
            try {
                System.setIn(new ByteArrayInputStream(new byte[0]));
                Assertions.assertDoesNotThrow(client::run);
            } finally {
                System.setIn(originalIn);
            }

            Mockito.verify(mockedConnection, Mockito.never()).sendMessage(Mockito.anyString());
            Mockito.verify(mockedConnection, Mockito.times(1)).close();
        }
    }
}
