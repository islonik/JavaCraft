package my.javacraft.echo.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author Lipatov Nikita
 */
public class NettyServer {

    private int port;

    public NettyServer(int port) {
        this.port = port;

        System.out.println("Use next command: telnet localhost " + port);
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            ((ServerBootstrap)((ServerBootstrap)b
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class))
                    .handler(new LoggingHandler(LogLevel.INFO)))
                    .childHandler(new NettyServerInitializer());

            b.bind(port).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
