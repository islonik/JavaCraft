package my.javacraft.echo.single.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SingleServerTest {

    private static final int PORT = 19076;
    private static ExecutorService executorService;

    @BeforeAll
    static void startServer() throws InterruptedException {
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new SingleServer(PORT));
        // Give the server time to bind
        Thread.sleep(500);
    }

    @AfterAll
    static void stopServer() {
        executorService.shutdownNow();
    }

    private String sendAndReceive(Socket socket, String message)
            throws IOException, InterruptedException {
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        out.write(message.getBytes());
        out.flush();
        Thread.sleep(200);

        byte[] buf = new byte[1024];
        int len = in.read(buf);
        return new String(buf, 0, len).trim();
    }

    @Test
    void testServerAcceptsConnection() throws IOException {
        try (Socket socket = new Socket("localhost", PORT)) {
            Assertions.assertTrue(socket.isConnected());
        }
    }

    @Test
    void testServerAcceptsMultipleConnections() throws IOException {
        try (Socket s1 = new Socket("localhost", PORT);
             Socket s2 = new Socket("localhost", PORT);
             Socket s3 = new Socket("localhost", PORT)) {
            Assertions.assertTrue(s1.isConnected());
            Assertions.assertTrue(s2.isConnected());
            Assertions.assertTrue(s3.isConnected());
        }
    }

    @Test
    void testServerEchosMessage() throws IOException, InterruptedException {
        try (Socket socket = new Socket("localhost", PORT)) {
            socket.setSoTimeout(2000);

            String response = sendAndReceive(socket, "hello world");
            Assertions.assertEquals("Did you say 'hello world'?", response);
        }
    }

    @Test
    void testServerRespondsToBye() throws IOException, InterruptedException {
        try (Socket socket = new Socket("localhost", PORT)) {
            socket.setSoTimeout(2000);

            String response = sendAndReceive(socket, "bye");
            Assertions.assertEquals("Have a good day!", response);
        }
    }

    @Test
    void testServerRespondsToStats() throws IOException, InterruptedException {
        try (Socket socket = new Socket("localhost", PORT)) {
            socket.setSoTimeout(2000);

            String response = sendAndReceive(socket, "stats");
            Assertions.assertTrue(response.endsWith("simultaneously connected clients."));
        }
    }

}
