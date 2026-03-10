package my.javacraft.echo.standard.server.platform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PlatformServerTest {

    @Test
    void testStartUpClientShouldHandleClientConversation() throws Exception {
        PlatformServer server = new PlatformServer(0);
        try (Socket client = Mockito.mock(Socket.class)) {
            ByteArrayInputStream input = new ByteArrayInputStream("stats\r\nbye\r\n".getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            Mockito.when(client.getInputStream()).thenReturn(input);
            Mockito.when(client.getOutputStream()).thenReturn(output);
            Mockito.when(client.getPort()).thenReturn(11111);

            server.startUpClient(client);

            Assertions.assertEquals(
                    "Simultaneously connected clients: 1\r\nHave a good day!\r\n",
                    output.toString(StandardCharsets.UTF_8)
            );
            Mockito.verify(client).close();
        }
    }

    @Test
    void testStartUpClientShouldCloseSocketWhenInitializationFails() throws Exception {
        PlatformServer server = new PlatformServer(0);
        try (Socket client = Mockito.mock(Socket.class)) {
            Mockito.when(client.getInputStream()).thenThrow(new IOException("forced input failure"));

            Assertions.assertDoesNotThrow(() -> server.startUpClient(client));
            Mockito.verify(client).close();
        }
    }

    @Test
    void testStartUpClientShouldHandleSocketCloseFailureAfterInitializationError() throws Exception {
        PlatformServer server = new PlatformServer(0);
        try (Socket client = Mockito.mock(Socket.class)) {
            Mockito.when(client.getInputStream()).thenThrow(new IOException("forced input failure"));
            Mockito.doThrow(new IOException("forced close failure")).when(client).close();

            Assertions.assertDoesNotThrow(() -> server.startUpClient(client));
            Mockito.verify(client).close();
            Mockito.doNothing().when(client).close();
        }
    }
}
