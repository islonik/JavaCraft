package my.javacraft.echo.standard.client.async;

/**
 * @author Lipatov Nikita
 */
public class StandardAsyncClientApplication {

    // telnet localhost 8075
    public static void main(String[] args) {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8075;
        }

        new StandardAsyncClient("localhost", port).run();
    }
}
