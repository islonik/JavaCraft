package my.javacraft.echo.standard.server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class ServerThread implements Runnable {

    private static final AtomicInteger threads = new AtomicInteger(0);
    private Socket socket;
    private BufferedReader inStream = null;
    private BufferedWriter outStream = null;

    public ServerThread(Socket socket) {
        try {
            this.socket = socket;
            this.inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.outStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            log.info("Simultaneously connected clients : {}", threads.incrementAndGet());
        } catch (IOException ioe) {
            log.error(ioe.getLocalizedMessage(), ioe);
        }
    }

    @Override
    public void run() {
        try {
            // all incoming requests
            boolean isConnected = true;
            while (isConnected) {
                String request = inStream.readLine();

                // we should add a line terminator at the end of the response here to close the line
                String response = "";
                if (request == null) {
                    isConnected = false;
                } else if (request.isEmpty()) {
                    response = "Please type something.\r\n";
                } else if ("stats".equalsIgnoreCase(request)) {
                    response = "%s simultaneously connected clients.\r\n".formatted(threads.get());
                } else if ("bye".equalsIgnoreCase(request)) {
                    response = "Have a good day!\r\n";
                    isConnected = false;
                } else {
                    response = "Did you say '" + request + "'?\r\n";
                }

                System.out.printf("resp %s = %s", socket.getPort(), response);

                this.outStream.write(response);
                this.outStream.flush();
            }
            log.info("Client {} left ", socket.getPort());
            log.info("Simultaneously connected clients : {}", threads.decrementAndGet());
        } catch (IOException ioe) {
            log.error("run method: " + ioe.getLocalizedMessage(), ioe);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException ioe) {
                log.error(ioe.getMessage(), ioe);
            }
        }
    }

}
