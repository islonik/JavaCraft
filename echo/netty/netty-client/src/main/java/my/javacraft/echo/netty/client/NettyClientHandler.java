package my.javacraft.echo.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<String> {

    private static final int QUEUE_CAPACITY = 10;
    private static final long POLL_TIMEOUT_MS = 500;
    private final ArrayBlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (messageQueue.size() >= QUEUE_CAPACITY) {
            log.debug("Message was removed from the queue = '{}'", messageQueue.poll());
        }
        messageQueue.add(msg);
        log.info(msg);
    }

    public String getMessage() {
        try {
            return messageQueue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for message", e);
            return null;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Client error", cause);
        ctx.close();
    }
}
