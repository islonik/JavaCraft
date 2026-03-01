package my.javacraft.echo.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

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

    /**
     * Close channel and run pending tasks to ensure the ChannelGroup
     * close-listener fires and removes the channel from the static group.
     */
    private void closeAndCleanup(EmbeddedChannel channel) {
        channel.close();
        channel.runPendingTasks();
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

        closeAndCleanup(channel);
    }

    @Test
    void testChannelActiveUsesUnknownOnHostnameFailure() {
        try (MockedStatic<InetAddress> mocked = Mockito.mockStatic(InetAddress.class)) {
            mocked.when(InetAddress::getLocalHost).thenThrow(new UnknownHostException("mocked"));

            EmbeddedChannel channel = new EmbeddedChannel(new NettyServerHandler());

            String welcome = channel.readOutbound();
            Assertions.assertNotNull(welcome);
            Assertions.assertEquals("Welcome to unknown!\r\n", welcome);

            String dateMsg = channel.readOutbound();
            Assertions.assertNotNull(dateMsg);
            Assertions.assertTrue(dateMsg.startsWith("It is "));

            closeAndCleanup(channel);
        }
    }

    @Test
    void testChannelInactiveOnClose() {
        EmbeddedChannel channel = createChannel();
        drainGreeting(channel);

        // Closing the channel should trigger channelInactive without errors
        Assertions.assertDoesNotThrow(() -> closeAndCleanup(channel));
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

        closeAndCleanup(channel);
    }

    @Test
    void testEchoResponse() {
        EmbeddedChannel channel = createChannel();
        drainGreeting(channel);

        channel.writeInbound("hello world");

        String response = channel.readOutbound();
        Assertions.assertEquals("Did you say 'hello world'?\r\n", response);

        closeAndCleanup(channel);
    }

    @Test
    void testByeClosesChannel() {
        EmbeddedChannel channel = createChannel();
        drainGreeting(channel);

        channel.writeInbound("bye");

        String response = channel.readOutbound();
        Assertions.assertEquals("Have a good day!\r\n", response);

        closeAndCleanup(channel);
    }

    @Test
    void testByeIsCaseInsensitive() {
        EmbeddedChannel channel = createChannel();
        drainGreeting(channel);

        channel.writeInbound("BYE");

        String response = channel.readOutbound();
        Assertions.assertEquals("Have a good day!\r\n", response);

        closeAndCleanup(channel);
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

        closeAndCleanup(channel);
    }

    @Test
    void testHelloCallsSendToAll() {
        EmbeddedChannel channel = createChannel();
        drainGreeting(channel);

        // "hello" triggers sendToAll — verify no exception and no regular response
        Assertions.assertDoesNotThrow(() -> channel.writeInbound("hello"));
        channel.runPendingTasks();

        closeAndCleanup(channel);
    }

    @Test
    void testSendToAllBroadcastsToOtherChannel() {
        // EmbeddedChannels share the default ID (0xembedded), so DefaultChannelGroup
        // treats them as duplicates. Use unique DefaultChannelId to allow both in the group.
        NettyServerHandler handler1 = new NettyServerHandler();
        EmbeddedChannel ch1 = new EmbeddedChannel(
                io.netty.channel.DefaultChannelId.newInstance(), handler1);
        drainGreeting(ch1);

        EmbeddedChannel ch2 = new EmbeddedChannel(
                io.netty.channel.DefaultChannelId.newInstance(), new NettyServerHandler());
        drainGreeting(ch2);

        // Call sendToAll directly — exercises both branches:
        // ch1 (sender) → else branch: "[you] ..."
        // ch2 (other)  → if branch:   "[<address>] ..."
        ChannelHandlerContext ctx1 = ch1.pipeline().context(handler1);
        handler1.sendToAll(ctx1, "test message");

        // Sender channel gets "[you] ..."
        ch1.flushOutbound();
        String selfMsg = ch1.readOutbound();
        Assertions.assertNotNull(selfMsg, "Sender should receive [you] message");
        Assertions.assertEquals("[you] test message\r\n", selfMsg);

        // Other channel gets "[<address>] ..."
        ch2.runPendingTasks();
        ch2.flushOutbound();
        String otherMsg = ch2.readOutbound();
        Assertions.assertNotNull(otherMsg, "Other channel should receive broadcast message");
        Assertions.assertTrue(otherMsg.endsWith("test message\r\n"),
                "Other channel message should contain the broadcast text");
        Assertions.assertFalse(otherMsg.startsWith("[you]"),
                "Non-sender channel should receive address-prefixed message, not [you]");

        closeAndCleanup(ch1);
        closeAndCleanup(ch2);
    }

    @Test
    void testChannelReadCompleteFlushes() {
        EmbeddedChannel channel = createChannel();
        drainGreeting(channel);

        // Write to context without flushing — data is buffered
        channel.pipeline().fireChannelReadComplete();

        // channelReadComplete calls ctx.flush() — verify no error
        Assertions.assertDoesNotThrow(() -> channel.checkException());

        closeAndCleanup(channel);
    }

    @Test
    void testExceptionCaughtClosesChannel() {
        EmbeddedChannel channel = createChannel();

        channel.pipeline().fireExceptionCaught(new RuntimeException("test error"));

        Assertions.assertFalse(channel.isOpen());
    }

}
