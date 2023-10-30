package my.javacraft.echo.standard.server;

/**
 * @author Lipatov Nikita
 */
public class StandardServerApplication {

    // telnet localhost 8075
    public static void main(String[] args) {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8075;
        }

        new MultithreadedServer(port).run();
    }
}
