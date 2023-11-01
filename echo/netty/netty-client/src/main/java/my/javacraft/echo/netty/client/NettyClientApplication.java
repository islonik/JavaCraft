package my.javacraft.echo.netty.client;

import java.util.Optional;

/**
 * @author Lipatov Nikita
 * Example was took from official documentation.
 */
public class NettyClientApplication {

    public static void main(String[] args) throws Exception {
        int port = Optional.of(args)
                .filter(a -> args.length > 0)
                .map(a -> Integer.parseInt(a[0]))
                .orElse(8076);

        new NettyClient("localhost", port).run();
    }
}
