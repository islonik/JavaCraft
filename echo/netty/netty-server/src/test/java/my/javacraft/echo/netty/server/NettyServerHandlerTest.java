package my.javacraft.echo.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NettyServerHandlerTest {

    private EmbeddedChannel createChannel() {
        // Test the handler directly without codecs —
        // EmbeddedChannel passes Strings as-is without encoding to ByteBuf
        return new EmbeddedChannel(new NettyServerHandler());
    }

    private void drainGreeting(EmbeddedChannel channel) {
        channel.readOutbound(); // Welcome message
        channel.readOutbound(); // Date message
    }

    @Test
    void testChannelActiveWritesGreeting() {
        EmbeddedChannel channel = createChannel();

        String welcome = channel.readOutbound();
        Assertions.assertNotNull(welcome);
        Assertions.assertTrue(welcome.startsWith("Welcome to "));

        String dateMsg = channel.readOutbound();
        Assertions.assertNotNull(dateMsg);
        Assertions.assertTrue(dateMsg.startsWith("It is "));

        channel.close();
    }

    @Test
    void testChannelInactiveOnClose() {
        EmbeddedChannel channel = createChannel();
        drainGreeting(channel);

        // Closing the channel should trigger channelInactive without errors
        Assertions.assertDoesNotThrow(() -> channel.close());
    }

    @Test
    void testChannelInactivePropagatesEvent() {
        // Verify super.channelInactive(ctx) is called, propagating the event
        // to the next handler in the pipeline
        AtomicBoolean downstreamNotified = new AtomicBoolean(false);

        EmbeddedChannel channel = new EmbeddedChannel(
                new NettyServerHandler(),
                new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) {
                        downstreamNotified.set(true);
                    }
                }
        );
        drainGreeting(channel);

        channel.close();

        Assertions.assertTrue(downstreamNotified.get(),
                "channelInactive event should propagate to downstream handlers via super.channelInactive(ctx)");
    }

    @Test
    void testEmptyInputReturnsPleasTypeSomething() {
        EmbeddedChannel channel = createChannel();
        drainGreeting(channel);

        channel.writeInbound("");

        String response = channel.readOutbound();
        Assertions.assertEquals("Please type something.\r\n", response);

        channel.close();
    }

    @Test
    void testEchoResponse() {
        EmbeddedChannel channel = createChannel();
        drainGreeting(channel);

        channel.writeInbound("hello world");

        String response = channel.readOutbound();
        Assertions.assertEquals("Did you say 'hello world'?\r\n", response);

        channel.close();
    }

    @Test
    void testByeClosesChannel() {
        EmbeddedChannel channel = createChannel();
        drainGreeting(channel);

        channel.writeInbound("bye");

        String response = channel.readOutbound();
        Assertions.assertEquals("Have a good day!\r\n", response);

        channel.close();
    }

    @Test
    void testByeIsCaseInsensitive() {
        EmbeddedChannel channel = createChannel();
        drainGreeting(channel);

        channel.writeInbound("BYE");

        String response = channel.readOutbound();
        Assertions.assertEquals("Have a good day!\r\n", response);

        channel.close();
    }

    @Test
    void testStatsReturnsClientCount() {
        EmbeddedChannel channel = createChannel();
        drainGreeting(channel);

        channel.writeInbound("stats");

        String response = channel.readOutbound();
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.startsWith("Simultaneously connected clients:"));
        Assertions.assertTrue(response.endsWith("\r\n"));

        channel.close();
    }

    @Test
    void testHelloSendsBroadcastToSender() {
        EmbeddedChannel channel = createChannel();
        drainGreeting(channel);

        // "hello" triggers sendToAll — the sender gets "[you]" prefix
        channel.writeInbound("hello");

        String response = channel.readOutbound();
        Assertions.assertEquals("[you] hello everybody!\r\n", response);

        channel.close();
    }

    @Test
    void testExceptionCaughtClosesChannel() {
        EmbeddedChannel channel = createChannel();

        channel.pipeline().fireExceptionCaught(new RuntimeException("test error"));

        Assertions.assertFalse(channel.isOpen());
    }

}
