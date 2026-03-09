package my.javacraft.echo.standard.client.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;

public abstract class UserClient implements AutoCloseable {

    public static final int CONNECT_TIMEOUT_MILLIS = 1_000;
    public static final int MAX_QUEUED_RESPONSES = 128;

    private final String host;
    private final int port;

    public UserClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public abstract void sendMessage(String userInput);

    public abstract String readMessage();

    public abstract void close();

    /**
     * Runs the interactive user loop for console clients.
     * <p>
     * It repeatedly prompts for input, sends the message to the server, and prints one response.
     * The loop exits on EOF (for example Ctrl+D/Ctrl+Z) or when the user enters "bye".
     * Client resources are always closed in the {@code finally} block.
     */
    public void readUserMessages(Logger log) {
        log.info("Starting...");

        try (Reader inputStreamReader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
             BufferedReader stdIn = new BufferedReader(inputStreamReader)) {

            while (true) {
                System.out.print("type: ");
                String userInput = stdIn.readLine();
                if (userInput == null) {
                    // readLine() returns null when the input stream reaches end-of-file (EOF)
                    // for example, Ctrl+D (Unix/Mac) or Ctrl+Z (Windows) — the user signals EOF on the terminal
                    log.info("Detected end-of-file (EOF). Thread terminating...");
                    break;
                }
                // Send exactly what the user entered in the console.
                sendMessage(userInput);

                // Print one response line received from the server.
                System.out.println(readMessage());

                // Explicit user command to terminate the session.
                if ("bye".equalsIgnoreCase(userInput)) {
                    break;
                }
            }
        } catch (IllegalStateException e) {
            log.warn(
                    "Client loop stopped because connection to: '{}:{}' is not available: {}",
                    host, port, e.getMessage()
            );
        } catch (IOException e) {
            log.warn(
                    "Couldn't get I/O for the connection to: '{}:{}' because: {}",
                    host, port, e.getMessage()
            );
        } catch (Exception e) {
            log.error(
                    "Exception the connection to: '{}:{}' because: {}",
                    host, port, e.getMessage()
            );
        } finally {
            close();
        }
    }

}
