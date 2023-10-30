package my.javacraft.echo.standard.server;

import java.io.*;
import java.net.Socket;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class ServerThread extends Thread {

    private Socket socket;
    private BufferedReader inStream = null;
    private BufferedWriter outStream = null;

    public ServerThread(Socket socket) {
        try {
            this.socket = socket;
            this.inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.outStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException ioe) {
            log.error(ioe.getLocalizedMessage(), ioe);
        }
    }

    @Override
    public void run() {
        try {
            // all incoming requests
            while (true) {
                String request = inStream.readLine();

                String response = null;
                boolean close = false;
                if (request.isEmpty()) {
                    response = "Please type something.\r\n";
                } else if ("bye".equalsIgnoreCase(request)) {
                    response = "Have a good day!\r\n";
                    close = true;
                } else {
                    response = "Did you say '" + request + "'?\r\n";
                }

                System.out.printf("resp %s = %s%n", socket.getPort(), response);

                this.outStream.write(response);
                this.outStream.flush();

                if (close) {
                    throw new InterruptedException();
                }
            }
        } catch (IOException ioe) {
            log.error("run method: " + ioe.getLocalizedMessage(), ioe);
        } catch (InterruptedException ie) {
            log.error("Client {} left ", socket.getPort());
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException ioe) {
                System.err.println(ioe);
                log.error(ioe.getMessage(), ioe);
            }
        }
    }

}
