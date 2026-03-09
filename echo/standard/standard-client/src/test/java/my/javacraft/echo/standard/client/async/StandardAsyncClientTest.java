package my.javacraft.echo.standard.client.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class StandardAsyncClientTest {

    @Test
    void testConstructorAndDelegatingMethods() {
        AtomicReference<List<?>> constructorArguments = new AtomicReference<>();
        try (MockedConstruction<AsyncClientConnection> ignored = Mockito.mockConstruction(
                AsyncClientConnection.class,
                (mock, context) -> {
                    constructorArguments.set(new ArrayList<>(context.arguments()));

                    Mockito.when(mock.readMessage()).thenReturn("pong");
                    Mockito.when(mock.isConnected()).thenReturn(true);
                    Mockito.when(mock.isSocketClosed()).thenReturn(false);
                })) {
            StandardAsyncClient client = new StandardAsyncClient("async-client", "127.0.0.1", 8075);
            AsyncClientConnection mockedConnection = ignored.constructed().getFirst();

            client.sendMessage("ping");
            Assertions.assertEquals("pong", client.readMessage());
            Assertions.assertTrue(client.isConnected());
            Assertions.assertFalse(client.isSocketClosed());
            client.close();

            Assertions.assertEquals(List.of("async-client", "127.0.0.1", 8075), constructorArguments.get());
            Mockito.verify(mockedConnection, Mockito.times(1)).sendMessage("ping");
            Mockito.verify(mockedConnection, Mockito.times(1)).readMessage();
            Mockito.verify(mockedConnection, Mockito.times(1)).isConnected();
            Mockito.verify(mockedConnection, Mockito.times(1)).isSocketClosed();
            Mockito.verify(mockedConnection, Mockito.times(1)).close();
        }
    }

    @Test
    void testRunShouldDelegateToReadUserMessages() {
        try (MockedConstruction<AsyncClientConnection> ignored = Mockito.mockConstruction(AsyncClientConnection.class)) {
            StandardAsyncClient client = Mockito.spy(new StandardAsyncClient("async-client", "127.0.0.1", 8075));
            Mockito.doNothing().when(client).readUserMessages(Mockito.any());

            Assertions.assertDoesNotThrow(client::run);
            Mockito.verify(client, Mockito.times(1)).readUserMessages(Mockito.any());
        }
    }

}
