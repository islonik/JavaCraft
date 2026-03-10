package my.javacraft.echo.standard.server.common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class MultithreadedServerTest {

    @Test
    void testRunShouldAcceptClientAndDelegateStartUp() throws Exception {
        AtomicReference<Socket> startedClient = new AtomicReference<>();
        MultithreadedServer server = new MultithreadedServer(8080) {
            @Override
            public void startUpClient(Socket client) {
                startedClient.set(client);
            }
        };
        InetAddress loopback = InetAddress.getLoopbackAddress();
        try (Socket acceptedClient = Mockito.mock(Socket.class);
             MockedConstruction<ServerSocket> construction = Mockito.mockConstruction(
                     ServerSocket.class,
                     (mock, context) -> {
                         Mockito.when(mock.getInetAddress()).thenReturn(loopback);
                         Mockito.when(mock.getLocalPort()).thenReturn(8080);
                         Mockito.when(mock.accept()).thenReturn(acceptedClient).thenThrow(new IOException("stop"));
                     }
             )) {

            Assertions.assertDoesNotThrow(server::run);

            Assertions.assertSame(acceptedClient, startedClient.get(), "Accepted socket should be delegated");
            ServerSocket serverSocket = construction.constructed().getFirst();
            Mockito.verify(serverSocket, Mockito.times(2)).accept();
            Mockito.verify(serverSocket).close();
        }
    }

    @Test
    void testRunShouldIgnoreNullAcceptedClient() throws Exception {
        AtomicInteger startCalls = new AtomicInteger(0);
        MultithreadedServer server = new MultithreadedServer(9090) {
            @Override
            public void startUpClient(Socket client) {
                startCalls.incrementAndGet();
            }
        };
        InetAddress loopback = InetAddress.getLoopbackAddress();
        try (MockedConstruction<ServerSocket> construction = Mockito.mockConstruction(
                ServerSocket.class,
                (mock, context) -> {
                    Mockito.when(mock.getInetAddress()).thenReturn(loopback);
                    Mockito.when(mock.getLocalPort()).thenReturn(9090);
                    Mockito.when(mock.accept()).thenReturn(null).thenThrow(new IOException("stop"));
                }
        )) {

            Assertions.assertDoesNotThrow(server::run);
            Assertions.assertEquals(0, startCalls.get(), "Null accepted sockets should be ignored");

            ServerSocket serverSocket = construction.constructed().getFirst();
            Mockito.verify(serverSocket, Mockito.times(2)).accept();
            Mockito.verify(serverSocket).close();
        }
    }
}
