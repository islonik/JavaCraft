package my.javacraft.echo.single.client;

/**
 * @author Lipatov Nikita
 */
public class SingleClientApplication {

    // telnet localhost 8077
    public static void main(String[] args) {
        SingleClient singleClient = new SingleClient("localhost", 8077);
        singleClient.run();
    }

}
