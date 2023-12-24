package my.javacraft.echo.single.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class SingleMessageSender {

    private volatile SelectionKey key;

    public void setKey(SelectionKey key) {
        synchronized (this) {
            this.key = key;
            notifyAll();
        }
    }

    public void send(String command) {
        try {
            if (key == null) {
                synchronized (this) {
                    while(key == null) {
                        wait();
                    }
                }
            }
            key.interestOps(SelectionKey.OP_WRITE);

            SocketChannel channel = (SocketChannel)key.channel();
            ByteBuffer writeBuffer = ByteBuffer.wrap(command.getBytes());
            channel.write(writeBuffer);

            key.interestOps(SelectionKey.OP_READ);

        } catch (IOException | InterruptedException ie) {
            log.error(ie.getMessage(), ie);
        }
    }
}
