package my.javacraft.echo.single.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Wait() / notifyAll() - notes:
 * When threads try to get socket(method getSocket()) then the threads will stop until the Main thread will open socket(method openSocket()).
 * + double checked locking
 * @author Lipatov Nikita
 */
@Slf4j
public class SingleNetworkManager {

    private static final int QUEUE_CAPACITY = 10;
    private static final long POLL_TIMEOUT_MS = 1_000;
    private static final long WAIT_TIMEOUT_MS = 2_000;
    private final ArrayBlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private volatile SocketChannel client;
    private volatile Selector selector;
    @Setter
    @Getter
    private SingleMessageSender singleMessageSender;

    public void openSocket(String serverHost, int serverPort) throws IOException {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    SocketChannel openedClient = null;
                    Selector openedSelector = null;
                    try {
                        // Finish the TCP handshake before publishing the connection to callers.
                        openedClient = openSocketChannel();
                        openedClient.connect(new InetSocketAddress(serverHost, serverPort));
                        openedClient.configureBlocking(false);

                        openedSelector = openSelector();
                        SelectionKey key = openedClient.register(openedSelector, SelectionKey.OP_READ);

                        client = openedClient;
                        selector = openedSelector;
                        publishReadyKey(key, openedSelector);
                        notifyAll();
                    } catch (IOException e) {
                        IOException closeFailure = closeResources(openedSelector, openedClient);
                        if (closeFailure != null) {
                            e.addSuppressed(closeFailure);
                        }
                        throw e;
                    }
                }
            }
        }
    }

    public Selector getSelector() {
        if (selector == null) {
            synchronized (this) {
                long deadline = System.currentTimeMillis() + WAIT_TIMEOUT_MS;
                while (selector == null) {
                    long remaining = deadline - System.currentTimeMillis();
                    if (remaining <= 0) {
                        throw new RuntimeException("Timed out waiting for selector");
                    }
                    try {
                        wait(remaining);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }
            }
        }
        return selector;
    }

    public SocketChannel getSocketChannel() {
        if (client == null) {
            synchronized (this) {
                long deadline = System.currentTimeMillis() + WAIT_TIMEOUT_MS;
                while (client == null) {
                    long remaining = deadline - System.currentTimeMillis();
                    if (remaining <= 0) {
                        throw new RuntimeException("Timed out waiting for socket channel");
                    }
                    try {
                        wait(remaining);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return client;
    }

    public void addMessage(String message) {
        while (!messageQueue.offer(message)) {
            messageQueue.poll();
        }
    }

    public String getMessage() {
        try {
            return messageQueue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public void closeSocket() {
        if (client != null) {
            synchronized (this) {
                if (client != null) {
                    Selector selectorToClose = selector;
                    SocketChannel clientToClose = client;

                    client = null;
                    selector = null;
                    publishReadyKey(null, null);

                    IOException closeFailure = closeResources(selectorToClose, clientToClose);
                    if (closeFailure != null) {
                        log.error(closeFailure.getMessage(), closeFailure);
                    }
                }
            }
        }
    }

    /**
     * Keeps channel creation overridable in tests so cleanup branches can be
     * exercised without depending on real network sockets.
     */
    SocketChannel openSocketChannel() throws IOException {
        return SocketChannel.open();
    }

    /**
     * Keeps selector creation overridable in tests for the same reason as
     * openSocketChannel().
     */
    Selector openSelector() throws IOException {
        return Selector.open();
    }

    /**
     * Keeps sender setup in one place so connection creation and teardown
     * always publish the same state to waiting senders.
     */
    private void publishReadyKey(SelectionKey key, Selector selector) {
        if (singleMessageSender != null) {
            singleMessageSender.setKey(key, selector);
        }
    }

    /**
     * Attempts to close every resource even when one close operation fails, so
     * cleanup cannot leak the remaining handles because of the first exception.
     */
    private IOException closeResources(Selector selector, SocketChannel channel) {
        IOException failure = null;
        failure = closeSelector(selector, failure);
        failure = closeChannel(channel, failure);
        return failure;
    }

    private IOException closeSelector(Selector selector, IOException failure) {
        if (selector == null) {
            return failure;
        }
        try {
            selector.close();
        } catch (IOException e) {
            return appendFailure(failure, e);
        }
        return failure;
    }

    private IOException closeChannel(SocketChannel channel, IOException failure) {
        if (channel == null) {
            return failure;
        }
        try {
            channel.close();
        } catch (IOException e) {
            return appendFailure(failure, e);
        }
        return failure;
    }

    private IOException appendFailure(IOException failure, IOException nextFailure) {
        if (failure == null) {
            return nextFailure;
        }
        failure.addSuppressed(nextFailure);
        return failure;
    }

}
