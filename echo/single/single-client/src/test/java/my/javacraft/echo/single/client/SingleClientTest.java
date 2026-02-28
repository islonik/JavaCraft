package my.javacraft.echo.single.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SingleClientTest {

    private static final int PORT = 19077;
    private static ServerSocket testServer;

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

    @AfterAll
    static void stopTestServer() throws IOException {
        if (testServer != null) {
            testServer.close();
        }
    }

    @Test
    void testConnectToServer() throws IOException {
        SingleClient client = new SingleClient("localhost", PORT);
        client.connectToServer();
        // If no exception thrown, connection succeeded
    }

    @Test
    void testSendAndReceiveEcho() throws IOException, InterruptedException {
        SingleClient client = new SingleClient("localhost", PORT);
        client.connectToServer();
        Thread.sleep(200);

        client.sendMessage("test message");
        Thread.sleep(200);
        String response = client.readMessage();
        Assertions.assertEquals("Did you say 'test message'?", response);
    }

    @Test
    void testSendByeReceivesGoodbye() throws IOException, InterruptedException {
        SingleClient client = new SingleClient("localhost", PORT);
        client.connectToServer();
        Thread.sleep(200);

        client.sendMessage("bye");
        Thread.sleep(200);
        String response = client.readMessage();
        Assertions.assertEquals("Have a good day!", response);
    }

}
