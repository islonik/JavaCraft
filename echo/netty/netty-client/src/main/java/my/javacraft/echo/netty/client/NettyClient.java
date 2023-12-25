package my.javacraft.echo.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class NettyClient {

    private final String host;
    private final int port;
    private final EventLoopGroup group;
    private volatile Channel ch;
    private NettyClientInitializer nettyClientInitializer;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.group = new NioEventLoopGroup();
    }

    public void openConnection() throws InterruptedException {
        this.nettyClientInitializer = new NettyClientInitializer();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(nettyClientInitializer);

        // Start the connection attempt & return opened channel.
        this.ch = b.connect(host, port).sync().channel();
    }

    public void sendMessage(String message) {
        ch.writeAndFlush(message + "\r\n");
    }

    public String readMessage() {
        return nettyClientInitializer.getClientHandler().getMessage();
    }

    public void run() {
        try {
            openConnection();

            // Read commands from the stdin.
            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (;;) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }

                // Sends the received line to the server.
                lastWriteFuture = ch.writeAndFlush(line + "\r\n");

                // If user typed the 'bye' command, wait until the server closes
                // the connection.
                if ("bye".equalsIgnoreCase(line)) {
                    ch.closeFuture().sync();
                    break;
                }
            }

            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } catch(InterruptedException | IOException ie) {
            log.error(ie.getMessage(), ie);
        } finally {
            group.shutdownGracefully();
        }
    }
}
