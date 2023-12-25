package my.javacraft.echo.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.ArrayBlockingQueue;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<String> {

    private static final int QUEUE_CAPACITY = 10;
    private final ArrayBlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (messageQueue.size() >= QUEUE_CAPACITY) {
            log.debug("Message was removed from the queue = '{}'", messageQueue.poll());
        }
        messageQueue.add(msg);
        System.out.println(msg);
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
