package my.javacraft.echo.single.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reads line-delimited server responses from the single client connection.
 * <p>
 * TCP is a byte stream, so responses can arrive fragmented across several reads or combined in a single read.
 * This listener keeps a small decode buffer and only publishes complete framed messages to the client queue.
 * <p>
 * @author Lipatov Nikita
 */
@Slf4j
@RequiredArgsConstructor
public class SingleMessageListener implements Runnable {

    static final int BUFFER_SIZE = 2 * 1024;
    static final long SELECTOR_TIMEOUT = 1_000L;
    private static final String MESSAGE_DELIMITER = "\r\n";

    private final SingleNetworkManager singleNetworkManager;
    private final StringBuilder responseBuffer = new StringBuilder();
    private final Deque<String> pendingMessages = new ArrayDeque<>();

    @Override
    public void run() {
        while (isNotInterrupted()) {
            Selector selector = singleNetworkManager.getSelector();
            if (selector == null) {
                log.info("Listener interrupted while waiting for selector.");
                break;
            }

            SingleMessageSender singleMessageSender = singleNetworkManager.getSingleMessageSender();

            try {
                while (isNotInterrupted()) {
                    selector.select(SELECTOR_TIMEOUT);
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                    while(keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();

                        processReadyKey(key, singleMessageSender);
                    }
                }
            } catch (IOException err) {
                log.error("IO error in listener, resetting connection", err);
                singleNetworkManager.closeSocket();
                singleMessageSender.setKey(null, null);
                break;
            } catch (Exception err) {
                // Handles ClosedSelectorException, CancelledKeyException, etc.
                // it's a normal BAU closing process, so the log level should be  'debug'
                log.debug("Listener loop terminated", err);
                singleNetworkManager.closeSocket();
                singleMessageSender.setKey(null, null);
                break;
            }
        }
        log.info("Listener thread terminated.");
    }

    private boolean isNotInterrupted() {
        return !Thread.currentThread().isInterrupted();
    }

    /**
     * Handles both writable and readable readiness for the same key because a
     * non-blocking socket can report both states in one selector cycle.
     */
    void processReadyKey(SelectionKey key, SingleMessageSender singleMessageSender) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        if (key.isWritable()) {
            singleMessageSender.flushPendingWrites();
        }
        if (key.isReadable()) {
            queueAvailableResponses(channel);
        }
    }

    /**
     * Drains every complete response that is already buffered for the socket.
     * This avoids losing coalesced frames when the server sends multiple
     * responses in a single TCP packet.
     */
    private void queueAvailableResponses(SocketChannel channel) {
        while (true) {
            String message = newResponse(channel);
            if (message == null) {
                return;
            }
            singleNetworkManager.addMessage(message);
            log.info(message);
        }
    }

    /**
     * Returns the next complete response without stripping meaningful
     * whitespace from the payload. Only the framing delimiter is removed.
     */
    public String newResponse(SocketChannel channel) {
        String bufferedMessage = pollPendingMessage();
        if (bufferedMessage != null) {
            return bufferedMessage;
        }

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        try {
            while (true) {
                int numRead = channel.read(buffer); // get message from client

                if (numRead == -1) {
                    log.debug("Connection closed by: {}", channel.getRemoteAddress());
                    channel.close();
                    return null;
                }

                if (numRead == 0) {
                    return pollPendingMessage();
                }

                buffer.flip();
                byte[] data = new byte[numRead];
                buffer.get(data);
                buffer.clear();

                responseBuffer.append(new String(data, StandardCharsets.UTF_8));
                extractCompleteFrames();

                String nextMessage = pollPendingMessage();
                if (nextMessage != null) {
                    return nextMessage;
                }
            }
        } catch (ClosedChannelException e) {
            return null;
        } catch (IOException e) {
            log.error("Unable to read from channel", e);
            try {
                channel.close();
            } catch (IOException e1) {
                //nothing to do, channel dead
            }
        }
        return null;
    }

    /**
     * Splits the accumulated stream into complete protocol frames while leaving
     * any unfinished suffix in the buffer for the next read.
     */
    private void extractCompleteFrames() {
        int delimiterIndex = responseBuffer.indexOf(MESSAGE_DELIMITER);
        while (delimiterIndex >= 0) {
            int frameEnd = delimiterIndex + MESSAGE_DELIMITER.length();
            pendingMessages.addLast(responseBuffer.substring(0, frameEnd));
            responseBuffer.delete(0, frameEnd);
            delimiterIndex = responseBuffer.indexOf(MESSAGE_DELIMITER);
        }
    }

    /**
     * Normalizes buffered frames for callers by removing only the protocol
     * delimiter, not leading/trailing spaces that belong to the payload.
     */
    private String pollPendingMessage() {
        String framedMessage = pendingMessages.pollFirst();
        if (framedMessage == null) {
            return null;
        }
        return trimTrailingLineDelimiters(framedMessage);
    }

    private String trimTrailingLineDelimiters(String message) {
        int end = message.length();
        while (end > 0) {
            char current = message.charAt(end - 1);
            if (current != '\r' && current != '\n') {
                break;
            }
            end--;
        }
        return message.substring(0, end);
    }
}
