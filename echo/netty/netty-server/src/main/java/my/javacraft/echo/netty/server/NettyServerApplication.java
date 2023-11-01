package my.javacraft.echo.netty.server;

import java.util.Optional;

/**
 * @author Lipatov Nikita
 * Example was took from official documentation.
 */
public class NettyServerApplication {

    // telnet localhost 8078
    public static void main(String[] args) throws Exception {
        int port = Optional.of(args)
                .filter(a -> args.length > 0)
                .map(a -> Integer.parseInt(a[0]))
                .orElse(8076);

        new NettyServer(port).run();
    }

}
