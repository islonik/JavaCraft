package my.javacraft.echo.netty.client;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NettyClientHandlerTest {

    @Test
    void testMessageIsQueuedOnChannelRead() {
        NettyClientHandler handler = new NettyClientHandler();
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        channel.writeInbound("Hello from server");

        String message = handler.getMessage();
        Assertions.assertEquals("Hello from server", message);

        channel.close();
    }

    @Test
    void testMultipleMessagesAreQueuedInOrder() {
        NettyClientHandler handler = new NettyClientHandler();
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        channel.writeInbound("first");
        channel.writeInbound("second");
        channel.writeInbound("third");

        Assertions.assertEquals("first", handler.getMessage());
        Assertions.assertEquals("second", handler.getMessage());
        Assertions.assertEquals("third", handler.getMessage());

        channel.close();
    }

    @Test
    void testGetMessageReturnsNullWhenQueueIsEmpty() {
        NettyClientHandler handler = new NettyClientHandler();
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        // No messages written — getMessage should return null after timeout
        String message = handler.getMessage();
        Assertions.assertNull(message);

        channel.close();
    }

    @Test
    void testQueueOverflowEvictsOldestMessage() {
        NettyClientHandler handler = new NettyClientHandler();
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        // Fill the queue to capacity (10)
        for (int i = 1; i <= 10; i++) {
            channel.writeInbound("message-" + i);
        }

        // Write one more — should evict the oldest (message-1)
        channel.writeInbound("message-11");

        // First available message should be "message-2" (message-1 was evicted)
        Assertions.assertEquals("message-2", handler.getMessage());

        // Remaining messages should follow in order
        for (int i = 3; i <= 11; i++) {
            Assertions.assertEquals("message-" + i, handler.getMessage());
        }

        channel.close();
    }

    @Test
    void testExceptionCaughtClosesChannel() {
        NettyClientHandler handler = new NettyClientHandler();
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        channel.pipeline().fireExceptionCaught(new RuntimeException("test error"));

        Assertions.assertFalse(channel.isOpen());
    }

}
