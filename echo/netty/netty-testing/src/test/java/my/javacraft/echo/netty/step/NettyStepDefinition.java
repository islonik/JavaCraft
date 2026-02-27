package my.javacraft.echo.netty.step;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import my.javacraft.echo.netty.client.NettyClient;
import my.javacraft.echo.netty.server.NettyServer;
import org.junit.jupiter.api.Assertions;

public class NettyStepDefinition {

    private final Map<String, NettyClient> connections = new ConcurrentHashMap<>();
    private NettyServer server;

    @Given("socket server started up on port = '{int}'")
    public void startUpSocketServer(int port) throws InterruptedException {
        server = new NettyServer(port);
        server.start();
        // Give the server a moment to bind
        Thread.sleep(200);
    }

    @When("create a new client {string} for the server with the port = '{int}'")
    public void createANewClientForTheServerWithThePort(String client, int port) throws InterruptedException {
        NettyClient nettyClient = new NettyClient("localhost", port);
        nettyClient.openConnection();
        // Wait for the server greeting messages to arrive
        Thread.sleep(200);
        // Drain the greeting messages (Welcome + date)
        nettyClient.readMessage();
        nettyClient.readMessage();
        connections.putIfAbsent(client, nettyClient);
    }

    @When("use the client {string} to send {string} message and get {string} response")
    public void sendMessage(String client, String message, String expectedResponse) throws InterruptedException {
        NettyClient nettyClient = connections.get(client);

        nettyClient.sendMessage(message);
        // Wait for the response
        Thread.sleep(200);
        String actualResponse = nettyClient.readMessage();

        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Then("close the connection to the client {string}")
    public void closeConnection(String client) throws InterruptedException {
        NettyClient nettyClient = connections.get(client);

        nettyClient.sendMessage("bye");
        Thread.sleep(200);
        String actualResponse = nettyClient.readMessage();
        Assertions.assertEquals("Have a good day!", actualResponse);

        nettyClient.close();
        connections.remove(client);
    }

}
