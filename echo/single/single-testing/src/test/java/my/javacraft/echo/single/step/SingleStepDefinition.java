package my.javacraft.echo.single.step;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import my.javacraft.echo.single.client.SingleClient;
import my.javacraft.echo.single.server.SingleServer;
import org.junit.jupiter.api.Assertions;

public class SingleStepDefinition {

    private final Map<String, SingleClient> connections = new ConcurrentHashMap<>();
    private final List<ExecutorService> serverExecutors = new ArrayList<>();

    @After
    public void cleanup() {
        connections.values().forEach(SingleClient::close);
        connections.clear();
        serverExecutors.forEach(ExecutorService::shutdownNow);
        serverExecutors.clear();
    }

    @Given("socket server started up on port = '{int}'")
    public void startUpSocketServer(int port) {
        SingleServer server = new SingleServer(port);

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(server);
        serverExecutors.add(executorService);
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

        Assertions.assertEquals(expectedResponse, actualResponse,
                "Client '%s' sent '%s' but got unexpected response".formatted(client, message));
    }

    @Then("close the connection to the client {string}")
    public void closeClientConnection(String client) {
        SingleClient singleClient = connections.get(client);

        singleClient.sendMessage("bye");
        String actualResponse = singleClient.readMessage();
        Assertions.assertEquals("Have a good day!", actualResponse,
                "Client '%s' did not receive expected goodbye response".formatted(client));
    }


}
