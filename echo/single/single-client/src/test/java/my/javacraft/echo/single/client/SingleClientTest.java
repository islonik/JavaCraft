package my.javacraft.echo.single.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class SingleClientTest {

    private static final int PORT = 19077;
    private static ServerSocket testServer;
    private InputStream originalIn;

    @BeforeAll
    static void startTestServer() throws IOException {
        testServer = new ServerSocket(PORT);
        Thread serverThread = new Thread(() -> {
            while (!testServer.isClosed()) {
                try {
                    Socket client = testServer.accept();
                    Thread handler = new Thread(() -> handleClient(client));
                    handler.setDaemon(true);
                    handler.start();
                } catch (IOException e) {
                    break;
                }
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private static void handleClient(Socket client) {
        try {
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                String msg = new String(buf, 0, len).trim();
                if (msg.isEmpty()) {
                    continue;
                }
                String response;
                if ("bye".equalsIgnoreCase(msg)) {
                    response = "Have a good day!";
                    out.write(response.getBytes());
                    out.flush();
                    client.close();
                    return;
                } else {
                    response = "Did you say '" + msg + "'?";
                    out.write(response.getBytes());
                    out.flush();
                }
            }
        } catch (IOException e) {
            // client disconnected
        }
    }

    @BeforeEach
    void saveStdin() {
        originalIn = System.in;
    }

    @AfterEach
    void restoreStdin() {
        System.setIn(originalIn);
    }

    @AfterAll
    static void stopTestServer() throws IOException {
        if (testServer != null) {
            testServer.close();
        }
    }

    @Test
    void testConnectToServer() throws IOException {
        SingleClient client = new SingleClient("localhost", PORT);
        try {
            client.connectToServer();
            // If no exception thrown, connection succeeded
        } finally {
            client.close();
        }
    }

    @Test
    void testSendAndReceiveEcho() throws IOException, InterruptedException {
        SingleClient client = new SingleClient("localhost", PORT);
        try {
            client.connectToServer();
            Thread.sleep(200);

            client.sendMessage("test message");
            Thread.sleep(200);
            String response = client.readMessage();
            Assertions.assertEquals("Did you say 'test message'?", response);
        } finally {
            client.close();
        }
    }

    @Test
    void testSendByeReceivesGoodbye() throws IOException, InterruptedException {
        SingleClient client = new SingleClient("localhost", PORT);
        try {
            client.connectToServer();
            Thread.sleep(200);

            client.sendMessage("bye");
            Thread.sleep(200);
            String response = client.readMessage();
            Assertions.assertEquals("Have a good day!", response);
        } finally {
            client.close();
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testRunWithInputThenEof() {
        System.setIn(new ByteArrayInputStream("hello\n".getBytes()));
        SingleClient client = new SingleClient("localhost", PORT);
        Assertions.assertDoesNotThrow(client::run);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testRunWithEmptyStdin() {
        // Empty stdin → readLine() returns null immediately → breaks out of loop
        System.setIn(new ByteArrayInputStream(new byte[0]));
        SingleClient client = new SingleClient("localhost", PORT);
        Assertions.assertDoesNotThrow(client::run);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testRunWithByeCommand() {
        System.setIn(new ByteArrayInputStream("bye\n".getBytes()));
        SingleClient client = new SingleClient("localhost", PORT);
        Assertions.assertDoesNotThrow(client::run);
    }

    @Test
    void testReadMessageReturnsNullWhenNoMessage() throws IOException {
        SingleClient client = new SingleClient("localhost", PORT);
        try {
            client.connectToServer();
            // No messages sent → queue is empty → returns null after poll timeout
            String msg = client.readMessage();
            Assertions.assertNull(msg);
        } finally {
            client.close();
        }
    }

    @Test
    void testMultipleRoundTrips() throws IOException, InterruptedException {
        SingleClient client = new SingleClient("localhost", PORT);
        try {
            client.connectToServer();
            Thread.sleep(200);

            client.sendMessage("first");
            Thread.sleep(200);
            String r1 = client.readMessage();
            Assertions.assertEquals("Did you say 'first'?", r1);

            client.sendMessage("second");
            Thread.sleep(200);
            String r2 = client.readMessage();
            Assertions.assertEquals("Did you say 'second'?", r2);
        } finally {
            client.close();
        }
    }

    @Test
    void testCloseIsIdempotent() throws IOException {
        SingleClient client = new SingleClient("localhost", PORT);
        client.connectToServer();

        // Calling close() twice should not throw
        Assertions.assertDoesNotThrow(() -> {
            client.close();
            client.close();
        });
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testRunWithMultipleMessages() {
        // Two messages then EOF — exercises the run() while loop multiple iterations
        System.setIn(new ByteArrayInputStream("first\nsecond\n".getBytes()));
        SingleClient client = new SingleClient("localhost", PORT);
        Assertions.assertDoesNotThrow(client::run);
    }

    @Test
    void testCloseWithoutConnect() {
        // Client created but never connected — close should not throw
        SingleClient client = new SingleClient("localhost", PORT);
        Assertions.assertDoesNotThrow(client::close);
    }

    @Test
    void testSendAndReadMultipleMessagesSameClient() throws IOException, InterruptedException {
        SingleClient client = new SingleClient("localhost", PORT);
        try {
            client.connectToServer();
            Thread.sleep(200);

            // Send 3 messages and verify all 3 responses
            for (int i = 1; i <= 3; i++) {
                client.sendMessage("msg" + i);
                Thread.sleep(200);
                String response = client.readMessage();
                Assertions.assertEquals("Did you say 'msg" + i + "'?", response);
            }
        } finally {
            client.close();
        }
    }

    // ── Tests for defensive catch blocks ─────────────────────────────

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testRunInnerCatchIOException() {
        // Covers run() inner catch(IOException) L75-77
        // Custom InputStream that throws IOException once, then returns EOF
        InputStream throwOnceStream = new InputStream() {
            private boolean thrown = false;
            @Override
            public int read() throws IOException {
                if (!thrown) {
                    thrown = true;
                    throw new IOException("stdin read failed");
                }
                return -1; // EOF to break the while loop
            }
        };
        System.setIn(throwOnceStream);

        SingleClient client = new SingleClient("localhost", PORT);
        // run() should catch the IOException, then readLine() returns null → breaks
        Assertions.assertDoesNotThrow(client::run);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testRunOuterCatchException() {
        // Covers run() outer catch(Exception) L79-80
        // Use a dead port so connectToServer() fails
        int deadPort;
        try (ServerSocket temp = new ServerSocket(0)) {
            deadPort = temp.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.setIn(new ByteArrayInputStream("hello\n".getBytes()));
        // The client connects to dead port — NIO connect is async, but
        // the subsequent send() will eventually fail or the listener will
        // detect the connection failure. We use a mock approach instead.
        // Use reflection to inject a mock that throws on openSocket()
        SingleClient client = new SingleClient("localhost", PORT);
        try {
            java.lang.reflect.Field mgrField = SingleClient.class.getDeclaredField("singleNetworkManager");
            mgrField.setAccessible(true);
            SingleNetworkManager mockMgr = mock(SingleNetworkManager.class);
            doThrow(new IOException("connection refused")).when(mockMgr).openSocket(anyString(), anyInt());
            mgrField.set(client, mockMgr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // run() calls connectToServer() which calls mockMgr.openSocket() → throws
        // The outer catch(Exception) should catch it
        Assertions.assertDoesNotThrow(client::run);
    }

}
