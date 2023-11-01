package my.javacraft.echo.netty.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author Lipatov Nikita
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel>
{
    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();

    public void initChannel(SocketChannel ch) throws Exception
    {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new ChannelHandler[] { new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()) });

        pipeline.addLast(new ChannelHandler[] { DECODER });
        pipeline.addLast(new ChannelHandler[] { ENCODER });

        /**
         *
         */
        pipeline.addLast(new ChannelHandler[] { new NettyServerHandler() });
    }
}