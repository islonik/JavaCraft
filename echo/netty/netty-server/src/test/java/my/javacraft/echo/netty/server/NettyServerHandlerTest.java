package my.javacraft.echo.netty.server;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NettyServerHandlerTest {

    private EmbeddedChannel createChannel() {
        // Test the handler directly without codecs —
        // EmbeddedChannel passes Strings as-is without encoding to ByteBuf
        return new EmbeddedChannel(new NettyServerHandler());
    }

    @Test
    void testEmptyInputReturnsPleasTypeSomething() {
        EmbeddedChannel channel = createChannel();

        // Drain greeting messages from channelActive
        channel.readOutbound();
        channel.readOutbound();

        channel.writeInbound("");

        String response = channel.readOutbound();
        Assertions.assertEquals("Please type something.\r\n", response);

        channel.close();
    }

    @Test
    void testEchoResponse() {
        EmbeddedChannel channel = createChannel();

        channel.readOutbound();
        channel.readOutbound();

        channel.writeInbound("hello world");

        String response = channel.readOutbound();
        Assertions.assertEquals("Did you say 'hello world'?\r\n", response);

        channel.close();
    }

    @Test
    void testByeClosesChannel() {
        EmbeddedChannel channel = createChannel();

        channel.readOutbound();
        channel.readOutbound();

        channel.writeInbound("bye");

        String response = channel.readOutbound();
        Assertions.assertEquals("Have a good day!\r\n", response);

        channel.close();
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

}
