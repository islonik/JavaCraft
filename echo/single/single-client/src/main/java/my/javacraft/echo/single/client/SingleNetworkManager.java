package my.javacraft.echo.single.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
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
    private final ArrayBlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private volatile SocketChannel client;
    private volatile Selector selector;
    @Setter
    @Getter
    private SingleMessageSender singleMessageSender;

    public void openSocket(String serverHost, String serverPort) throws IOException {
        if (client == null) {
            synchronized (this) {
                while(client == null) {
                    client = SocketChannel.open();
                    // nonblocking I/O
                    client.configureBlocking(false);
                    client.connect(new InetSocketAddress(serverHost, Integer.parseInt(serverPort)));
                    selector = Selector.open();
                    client.register(selector, SelectionKey.OP_CONNECT);

                    notifyAll();
                }
            }
        }
    }

    public Selector getSelector() {
        if (selector == null) {
            synchronized (this) {
                while(selector == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return selector;
    }

    public SocketChannel getSocketChannel() {
        if (client == null) {
            synchronized (this) {
                while(client == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return client;
    }

    public void addMessage(String message) {
        if (messageQueue.size() >= QUEUE_CAPACITY) {
            log.debug("Message was removed from the queue = {}", messageQueue.poll());
        }
        messageQueue.add(message);
    }

    public String getMessage() {
        if (messageQueue.peek() == null) {
            synchronized (this) {
                if (messageQueue.peek() == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return messageQueue.poll();
    }

    public void closeSocket() {
        try {
            if (client != null) {
                synchronized (this) {
                    while(client != null) {
                        selector.close();
                        client.socket().close();
                        client.close();
                        client = null;
                        selector = null;
                    }
                }
            }
        } catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
        }
    }


}
