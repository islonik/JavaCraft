package my.javacraft.echo.standard.client.async;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class AsyncClientConnection extends Thread {

    private Socket socket = null;
    private BufferedReader inStream = null;
    private PrintWriter outStream = null;

    public AsyncClientConnection(String host, int port) {
        try {
            socket = new Socket(host, port);
            outStream = new PrintWriter(socket.getOutputStream(), true);
            inStream  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            log.info("Async client {} is connected", socket);
        } catch (Exception error) {
            log.error(error.getMessage(), error);
        }
    }

    /**
     * Method sends messages / commands to server.
     * @param message Message from user / to server.
     */
    public void flush(String message) {
        try {
            outStream.println(message);
        } catch (Exception error) {
            System.err.println("flush method: " + error);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = inStream.readLine();
                if (message == null) {
                    return;
                }
                System.out.println(message);
            }
        } catch (EOFException error) {
            log.error("Connection to the server was lost");
        } catch (SocketException error) {
            log.error("The server was shut down");
        } catch (Exception error) {
            log.error("Fatal fail in run method because: " + error);
        } finally {
            try {
                kill();
            } catch (Exception error) {
                log.error("finally of run method:" + error);
            }
        }
    }

    /**
     * Method kills the object of client connection.
     */
    public void kill() {
        try {
            if (socket != null) {
                if (!socket.isClosed()) {
                    socket.close();
                }
                socket = null;
            }
            this.interrupt();
        } catch (Exception error) {
            System.err.println("Close method:" + error);
        }
    }
}
