package my.javacraft.echo.netty.server;

import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.net.InetAddress;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
        ctx.write("Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\n");
        ctx.write("It is " + LocalDateTime.now() + " now.\r\n");
        ctx.flush();

        channels.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client disconnected: {}", ctx.channel().remoteAddress());
        // ChannelGroup automatically removes closed channels,
        // so no manual removal is needed here.
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) {
        // Generate and write a response.
        String response;
        boolean close = false;
        if (request.isEmpty()) {
            response = "Please type something.\r\n";
        } else if ("bye".equalsIgnoreCase(request)) {
            response = "Have a good day!\r\n";
            close = true;
        } else if ("hello".equalsIgnoreCase(request)) {
            sendToAll(ctx, "hello everybody!");
            return;
        } else if ("stats".equalsIgnoreCase(request)) {
            response = "Simultaneously connected clients: %s\r\n".formatted(channels.size());
        } else {
            response = "Did you say '" + request + "'?\r\n";
        }
        // We do not need to write a ChannelBuffer here.
        // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
        ChannelFuture future = ctx.writeAndFlush(response);
        // Close the connection after sending 'Have a good day!'
        // if the client has sent 'bye'.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public void sendToAll(ChannelHandlerContext ctx, String msg) {
        for (Channel c : channels) {
            if (c != ctx.channel()) {
                c.writeAndFlush("[" + ctx.channel().remoteAddress() + "] " + msg + "\r\n");
            } else {
                c.writeAndFlush("[you] " + msg + "\r\n");
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Unexpected error", cause);
        ctx.close();
    }
}
