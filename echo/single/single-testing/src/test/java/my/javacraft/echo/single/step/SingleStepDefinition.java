package my.javacraft.echo.single.step;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import my.javacraft.echo.single.client.SingleClient;
import my.javacraft.echo.single.server.SingleServer;
import org.junit.jupiter.api.Assertions;

public class SingleStepDefinition {

    private final Map<String, SingleClient> connections = new ConcurrentHashMap<>();

    @Given("socket server started up on port = '{int}'")
    public void startUpSocketServer(int port) {
        SingleServer server = new SingleServer(port);

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        // loop
        executorService.execute(server);
    }

    @When("create a new client {string} for the server with the port = '{int}'")
    public void createANewClientNikitaForTheServerWithThePort(String client, int port) throws IOException {
        SingleClient singleClient = new SingleClient("localhost", port);
        singleClient.connectToServer();
        connections.putIfAbsent(client, singleClient);
    }

    @When("use the client {string} to send {string} message and get {string} response")
    public void sendMessage(String client, String message, String expectedResponse) {
        SingleClient singleClient = connections.get(client);

        singleClient.sendMessage(message);
        String actualResponse = singleClient.readMessage();

        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Then("close the connection to the client {string}")
    public void sendMessage(String client) {
        SingleClient singleClient = connections.get(client);

        singleClient.sendMessage("bye");
        String actualResponse = singleClient.readMessage();
        Assertions.assertEquals("Have a good day!", actualResponse);
    }


}
