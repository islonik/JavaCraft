package my.javacraft.echo.single.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends one line-delimited command at a time.
 * <p>
 * The transport is stream-based, so each payload must be framed explicitly
 * before it is written to the socket.
 * <p>
 * @author Lipatov Nikita
 */
@Slf4j
public class SingleMessageSender {

    private static final String MESSAGE_DELIMITER = "\r\n";
    private static final long SEND_WAIT_TIMEOUT_MS = 5_000;

    private volatile SelectionKey key;
    private volatile Selector selector;

    /**
     * Unblocks pending senders once the selector thread has finished the
     * connection handshake and published the key for the socket channel.
     */
    public void setKey(SelectionKey key, Selector selector) {
        synchronized (this) {
            this.key = key;
            this.selector = selector;
            notifyAll();
        }
    }

    /**
     * Frames the command with a line delimiter so the server can detect message
     * boundaries even when TCP splits or merges writes.
     */
    public void send(String command) {
        try {
            if (key == null) {
                synchronized (this) {
                    long deadline = System.currentTimeMillis() + SEND_WAIT_TIMEOUT_MS;
                    while (key == null) {
                        long remaining = deadline - System.currentTimeMillis();
                        if (remaining <= 0) {
                            log.warn("Timed out waiting for selection key");
                            return;
                        }
                        wait(remaining);
                    }
                }
            }

            SelectionKey currentKey = key;
            Selector currentSelector = selector;
            if (currentKey == null) {
                log.warn("Selection key became null before send");
                return;
            }

            currentKey.interestOps(SelectionKey.OP_WRITE);

            SocketChannel channel = (SocketChannel) currentKey.channel();
            String framedCommand = frameCommand(command);
            ByteBuffer writeBuffer = ByteBuffer.wrap(framedCommand.getBytes(StandardCharsets.UTF_8));
            while (writeBuffer.hasRemaining()) {
                channel.write(writeBuffer);
            }

            currentKey.interestOps(SelectionKey.OP_READ);
            if (currentSelector != null) {
                currentSelector.wakeup();
            }

        } catch (IOException | CancelledKeyException e) {
            log.error(e.getMessage(), e);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error(ie.getMessage(), ie);
        }
    }

    /**
     * Normalizes caller input to the single wire format used by the project.
     * Existing trailing line endings are preserved only when they already match
     * the protocol delimiter.
     */
    private String frameCommand(String command) {
        if (command.endsWith(MESSAGE_DELIMITER)) {
            return command;
        }
        if (command.endsWith("\n")) {
            return command.substring(0, command.length() - 1) + MESSAGE_DELIMITER;
        }
        if (command.endsWith("\r")) {
            return command + "\n";
        }
        return command + MESSAGE_DELIMITER;
    }
}
