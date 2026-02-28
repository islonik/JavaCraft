package my.javacraft.echo.single.client;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Lipatov Nikita
 */
@Slf4j
public class SingleClientApplication {

    // telnet localhost 8077
    public static void main(String[] args) {
        SingleClient singleClient = new SingleClient("localhost", 8077);
        try {
            singleClient.run();
        } catch (Exception e) {
            log.error("Client failure", e);
        } finally {
            singleClient.close();
        }
    }

}
