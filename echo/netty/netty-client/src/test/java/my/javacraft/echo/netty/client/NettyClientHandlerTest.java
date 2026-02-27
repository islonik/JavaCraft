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
    void testExceptionCaughtClosesChannel() {
        NettyClientHandler handler = new NettyClientHandler();
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        channel.pipeline().fireExceptionCaught(new RuntimeException("test error"));

        Assertions.assertFalse(channel.isOpen());
    }

}
