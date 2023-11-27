package my.javacraft.echo.standard.step;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import my.javacraft.echo.standard.client.sync.StandardSyncClient;
import my.javacraft.echo.standard.server.MultithreadedServer;
import org.junit.jupiter.api.Assertions;

public class StandardStepDefinition {

    private final Map<String, StandardSyncClient> connections = new ConcurrentHashMap<>();

    @Given("socket server started up on port = '{int}'")
    public void startUpSocketServer(int port) {
        MultithreadedServer server = new MultithreadedServer(port);

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        // loop
        executorService.execute(server);
    }

    @When("create a new client {string} for the server with the port = '{int}'")
    public void createANewClientNikitaForTheServerWithThePort(String client, int port) {
        connections.putIfAbsent(client, new StandardSyncClient("localhost", port));
    }

    @When("use the client {string} to send {string} message and get {string} response")
    public void sendMessage(String client, String message, String expectedResponse) throws Exception {
        StandardSyncClient syncClient = connections.get(client);

        syncClient.sendMessage(message);
        String actualResponse = syncClient.readMessage();

        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Then("close the connection to the client {string}")
    public void sendMessage(String client) throws Exception {
        StandardSyncClient syncClient = connections.get(client);

        syncClient.sendMessage("bye");
        String actualResponse = syncClient.readMessage();

        Assertions.assertEquals("Have a good day!", actualResponse);
    }


}
